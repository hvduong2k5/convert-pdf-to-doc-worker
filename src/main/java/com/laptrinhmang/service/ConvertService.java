package com.laptrinhmang.service;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.FileFormat;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class ConvertService {

    private static final String WATERMARK = "Evaluation Warning";

    public File convertFullProcess(File pdfFile, String tempDir) throws Exception {

        File dir = new File(tempDir);
        InputStream inputStream = Files.newInputStream(pdfFile.toPath());
        PdfDocument pdf = new PdfDocument(inputStream);
        if (!dir.exists() && !dir.mkdirs()) {
            boolean success = dir.mkdirs();
            if (!success) {
                throw new IOException("Failed to create directory: " + tempDir);
            }
        }
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

        return new File(cleanDocx);
    }

    // ------------------- TÁCH PDF (Spire.PDF) -------------------
    public String[] splitPdf(File inputFile, String dir, int pagesPerSplit) throws Exception {

        PdfDocument pdf = new PdfDocument();
        pdf.loadFromFile(inputFile.getAbsolutePath());

        int total = pdf.getPages().getCount();
        int parts = (total + pagesPerSplit - 1) / pagesPerSplit;
        String[] out = new String[parts];

        int idx = 0;

        for (int i = 0; i < total; i += pagesPerSplit) {

            PdfDocument part = new PdfDocument();
            try {
                int end = Math.min(i + pagesPerSplit, total);

                for (int p = i; p < end; p++) {
                    part.insertPage(pdf, p);
                }

                String outPath = dir + "/split_" + (idx + 1) + ".pdf";
                part.saveToFile(outPath);

                out[idx] = outPath;
                idx++;

            } finally {
                part.close();
                part.dispose();
            }
        }

        pdf.close();

        return out;
    }

    // ------------------- CONVERT PDF → DOCX -------------------
    public String[] convertChunks(String[] pdfs) throws Exception {

        String[] out = new String[pdfs.length];

        for (int i = 0; i < pdfs.length; i++) {

            PdfDocument doc = new PdfDocument();

            try {
                doc.loadFromFile(pdfs[i]);
                String output = pdfs[i].replace(".pdf", ".docx");
                doc.saveToFile(output, FileFormat.DOCX);
                out[i] = output;

            } finally {
                doc.close();
                doc.dispose();
            }
        }

        return out;
    }

    // ------------------- MERGE DOCX -------------------
    public void mergeDocxFiles(String[] docxFiles, String output) throws Exception {

        com.spire.doc.Document merged = new com.spire.doc.Document();

        try {

            for (String f : docxFiles) {
                com.spire.doc.Document doc = new com.spire.doc.Document();

                try {
                    doc.loadFromFile(f);
                    for (int j = 0; j < doc.getSections().getCount(); j++) {
                        merged.importSection(doc.getSections().get(j));
                    }

                } finally {
                    doc.close();
                    doc.dispose();
                }
            }

            merged.saveToFile(output, com.spire.doc.FileFormat.Docx);

        } finally {
            merged.close();
        }
    }

    // ------------------- XOÁ WATERMARK SAFE -------------------
    public void removeWatermark(String input, String output) throws Exception {
        ZipSecureFile.setMaxFileCount(100000);
        ZipSecureFile.setMinInflateRatio(0.0001);

        FileInputStream fis = null;
        FileOutputStream fos = null;
        XWPFDocument doc = null;

        try {
            fis = new FileInputStream(input);
            doc = new XWPFDocument(fis);

            for (XWPFParagraph p : doc.getParagraphs()) cleanParagraph(p);
            for (XWPFTable t : doc.getTables())
                for (XWPFTableRow r : t.getRows())
                    for (XWPFTableCell c : r.getTableCells())
                        for (XWPFParagraph p : c.getParagraphs())
                            cleanParagraph(p);

            fos = new FileOutputStream(output);
            doc.write(fos);

        } finally {
            if (doc != null) doc.close();
            if (fis != null) fis.close();
            if (fos != null) fos.close();
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
