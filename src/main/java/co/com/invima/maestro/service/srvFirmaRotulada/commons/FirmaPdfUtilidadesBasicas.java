package co.com.invima.maestro.service.srvFirmaRotulada.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class FirmaPdfUtilidadesBasicas {

    public FirmaPdfUtilidadesBasicas() {
        // TODO Auto-generated constructor stub
    }

    public static void agregarPaginaPDF(String inputPath, String outputPath) throws IOException, DocumentException {
        OutputStream fos = new FileOutputStream(new File(outputPath));

        PdfReader pdfReader = new PdfReader(inputPath);
        PdfStamper pdfStamper = new PdfStamper(pdfReader, fos);
        pdfStamper.insertPage(pdfReader.getNumberOfPages() + 1, pdfReader.getPageSizeWithRotation(1));
        pdfStamper.close();
        pdfStamper.flush();
        pdfReader.close();
        fos.close();
        fos.flush();
    }
}
