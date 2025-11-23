package com.laptrinhmang;

import com.google.gson.Gson;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.bean.Status;
import com.laptrinhmang.service.ConvertService;
import com.laptrinhmang.service.MinioService;
import com.laptrinhmang.service.RedisService;
import com.laptrinhmang.util.RedisUtil;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.FileFormat;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

public class App {

    private static final String WATERMARK = "Evaluation Warning";

    public static void main(String[] args) {


        File file = new File("A:/PBL4_Report.pdf");
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(1);
        fileEntity.setUserId(1);
        fileEntity.setName(file.getName());
        fileEntity.setLink_pdf(file.getName());
        fileEntity.setSize((int) file.length());
        fileEntity.setStatus(Status.PENDING);
        fileEntity.setCreated_at(LocalDateTime.now().toString());
        System.out.println(new Gson().toJson(fileEntity));
        RedisService redisService = new RedisService();
        redisService.push(fileEntity);
        MinioService minioService = new MinioService();
        try {
            minioService.upload(file);
            System.out.println("Upload xong: " + file.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
        FileEntity filePop = redisService.pop(RedisUtil.getQueueName());
        System.out.println(new Gson().toJson(filePop));
        File downloadedFile = null;
        try {
            downloadedFile = minioService.download(filePop.getLink_pdf());
            System.out.println("Download xong: " + downloadedFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConvertService convertService = new ConvertService();
        try {
            File resultFile = new File(convertService.convertFullProcess(downloadedFile, "A:/temp_convert"));
            minioService.upload(resultFile);
            System.out.println("Convert xong: " + resultFile.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String inputPdf = "A:/qew.pdf";                // PDF gốc
        String tempDir = "A:/temp_convert";              // thư mục tạm
        String mergedDocx = "A:/merged.docx";            // file DOCX gộp (Chứa Watermark)
        String cleanDocx = "A:/merged_clean.docx";       // file DOCX gộp cuối cùng (Sạch Watermark)

        int pagesPerSplit = 10;
//
//        File dir = new File(tempDir);
//        if (!dir.exists()) dir.mkdirs();
//
//        try {
//            System.out.println("1/4. Tách PDF...");
//            String[] pdfParts = splitPdf(inputPdf, tempDir, pagesPerSplit);
//
//            System.out.println("2/4. Convert từng phần sang DOCX...");
//            String[] docxParts = convertChunks(pdfParts);
//
//            System.out.println("3/4. Merge DOCX bằng GroupDocs.Merger...");
//            mergeDocxFiles(docxParts, mergedDocx);
//
//            // Tùy chọn: Xóa các tệp DOCX tạm thời sau khi hợp nhất thành công
//            for (String file : docxParts) {
//                new File(file).delete();
//            }
//
//            System.out.println("4/4. Xoá watermark...");
//            removeWatermark(mergedDocx, cleanDocx);
//
//            new File(mergedDocx).delete();
//
//            System.out.println("\n🎉 HOÀN THÀNH — File sạch: " + cleanDocx);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    // ------------------- TÁCH PDF (Sử dụng Spire.PDF) -------------------
    public static String[] splitPdf(String input, String dir, int pagesPerSplit) {
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(input);

        int totalPages = pdf.getPages().getCount();
        int numParts = (totalPages + pagesPerSplit - 1) / pagesPerSplit;

        String[] out = new String[numParts];
        int idx = 0;

        for (int i = 0; i < totalPages; i += pagesPerSplit) {
            PdfDocument part = new PdfDocument();

            int start = i;
            int end = Math.min(i + pagesPerSplit, totalPages);

            for (int p = start; p < end; p++) {
                part.insertPage(pdf, p);
            }

            String outPath = dir + "/split_" + (idx + 1) + ".pdf";
            part.saveToFile(outPath);
            part.close();

            out[idx] = outPath;

            System.out.println(" - PDF phần " + (idx + 1) + ": " + outPath);
            idx++;
        }

        pdf.close();
        return out;
    }

    // ------------------- CONVERT PDF → DOCX (Sử dụng Spire.PDF) -------------------
    public static String[] convertChunks(String[] pdfs) {
        String[] out = new String[pdfs.length];

        for (int i = 0; i < pdfs.length; i++) {
            String input = pdfs[i];
            String output = input.replace(".pdf", ".docx");

            PdfDocument doc = new PdfDocument();
            doc.loadFromFile(input);

            // Sửa lỗi: Chỉ định rõ định dạng đầu ra là DOCX
            doc.saveToFile(output, FileFormat.DOCX);

            doc.close();

            out[i] = output;
            System.out.println("Convert xong: " + output);
        }

        return out;
    }

    public static void mergeDocxFiles(String[] docxFiles, String outputDocx) throws Exception {
        // SỬ DỤNG FQN để tránh xung đột với org.apache.poi.xwpf.usermodel.Document
        com.spire.doc.Document mergedDoc = new com.spire.doc.Document();

        for (String docxFile : docxFiles) {
            com.spire.doc.Document doc = new com.spire.doc.Document();
            doc.loadFromFile(docxFile);

            for (int j = 0; j < doc.getSections().getCount(); j++) {
                com.spire.doc.Section section = doc.getSections().get(j);
                mergedDoc.importSection(section);
            }
        }
        mergedDoc.saveToFile(outputDocx, com.spire.doc.FileFormat.Docx);
    }

    // ------------------- XOÁ WATERMARK (Sử dụng Apache POI) -------------------
    public static void removeWatermark(String input, String output) throws Exception {

        try (FileInputStream fis = new FileInputStream(input);
             XWPFDocument doc = new XWPFDocument(fis)) {

            // Xoá watermark trong paragraphs
            for (XWPFParagraph p : doc.getParagraphs()) {
                cleanParagraph(p);
            }

            // Xoá watermark trong bảng
            for (XWPFTable t : doc.getTables()) {
                for (XWPFTableRow row : t.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            cleanParagraph(p);
                        }
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(output)) {
                doc.write(fos);
            }
        }

        System.out.println("Đã xoá watermark → " + output);
    }

    private static void cleanParagraph(XWPFParagraph p) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null) return;

        for (int i = runs.size() - 1; i >= 0; i--) {
            XWPFRun run = runs.get(i);
            // Chuỗi WATERMARK này cần được cập nhật nếu GroupDocs có watermark khác
            if (run.toString().contains(WATERMARK)) {
                p.removeRun(i);
            }
        }
    }
}