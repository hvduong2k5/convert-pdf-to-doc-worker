package com.laptrinhmang.service;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.FileFormat;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.List;

public class ConvertService {

    private static final String WATERMARK = "Evaluation Warning";

    public String convertFullProcess(File pdfFile, String tempDir) throws Exception {

        File dir = new File(tempDir);
        if (!dir.exists()) dir.mkdirs();
        // 1) Tách PDF thành nhiều file nhỏ
        String[] pdfParts = splitPdf(pdfFile, tempDir, 10);

        // 2) Convert từng phần sang DOCX
        String[] docxParts = convertChunks(pdfParts);

        // 3) Merge DOCX
        String mergedDocx = tempDir + "/merged.docx";
        mergeDocxFiles(docxParts, mergedDocx);

        // 4) Xóa watermark
        String cleanDocx = tempDir + "/clean.docx";
        removeWatermark(mergedDocx, cleanDocx);

        return cleanDocx;
    }

    // ------------------- TÁCH PDF (Spire.PDF) -------------------
    public String[] splitPdf(File inputFile, String dir, int pagesPerSplit) {
        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(inputFile.getAbsolutePath());
        int total = pdf.getPages().getCount();
        int parts = (total + pagesPerSplit - 1) / pagesPerSplit;

        String[] out = new String[parts];
        int idx = 0;

        for (int i = 0; i < total; i += pagesPerSplit) {

            PdfDocument part = new PdfDocument();
            int start = i;
            int end = Math.min(i + pagesPerSplit, total);

            for (int p = start; p < end; p++) {
                part.insertPage(pdf, p);
            }

            String outPath = dir + "/split_" + (idx + 1) + ".pdf";
            part.saveToFile(outPath);
            part.close();

            out[idx] = outPath;
            idx++;
        }
        pdf.close();
        return out;
    }

    // ------------------- CONVERT PDF → DOCX -------------------
    public String[] convertChunks(String[] pdfs) {
        String[] out = new String[pdfs.length];

        for (int i = 0; i < pdfs.length; i++) {
            String input = pdfs[i];
            String output = input.replace(".pdf", ".docx");

            PdfDocument doc = new PdfDocument();
            doc.loadFromFile(input);
            doc.saveToFile(output, FileFormat.DOCX);
            doc.close();

            out[i] = output;
        }

        return out;
    }

    // ------------------- MERGE DOCX -------------------
    public void mergeDocxFiles(String[] docxFiles, String outputDocx) throws Exception {

        com.spire.doc.Document mergedDoc = new com.spire.doc.Document();

        for (String docxFile : docxFiles) {
            com.spire.doc.Document doc = new com.spire.doc.Document();
            doc.loadFromFile(docxFile);

            for (int j = 0; j < doc.getSections().getCount(); j++) {
                com.spire.doc.Section sec = doc.getSections().get(j);
                mergedDoc.importSection(sec);
            }
        }
        mergedDoc.saveToFile(outputDocx, com.spire.doc.FileFormat.Docx);
    }

    // ------------------- XOÁ WATERMARK -------------------
    public void removeWatermark(String input, String output) throws Exception {

        try (FileInputStream fis = new FileInputStream(input);
             XWPFDocument doc = new XWPFDocument(fis)) {

            // Paragraphs
            for (XWPFParagraph p : doc.getParagraphs()) {
                cleanParagraph(p);
            }

            // Tables
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
    }

    private void cleanParagraph(XWPFParagraph p) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null) return;

        for (int i = runs.size() - 1; i >= 0; i--) {
            XWPFRun run = runs.get(i);
            if (run.toString().contains(WATERMARK)) {
                p.removeRun(i);
            }
        }
    }
}
