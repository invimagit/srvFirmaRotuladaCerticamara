package co.com.invima.maestro.service.srvFirmaRotulada.commons;

import java.io.File;
import java.nio.file.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.Date;
import java.util.Base64;
import java.nio.file.Files;

import ch.qos.logback.classic.Logger;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.BaseFont;

public class FirmaPDF {

    public FirmaPDF() {
        // TODO Auto-generated constructor stub
    }

    public static String GenerarFirma(String storagePath, String documentoPDF, String ImagenJPG, Integer numeroTramite,
                                      Integer numeroRadicado, Integer idUsuarioFirmante, String firmanteNombre, String firmanteTipoDocumento,
                                      String firmanteNumeroDocumento, String firmanteTelefono, String firmanteCorreo, String firmanteRool, String firmaImagen, String documentoPdf2)
            throws Exception {

        String oldinputFilePath = storagePath + "InputFile" + numeroTramite + numeroRadicado + idUsuarioFirmante
                + ".pdf";
        String inputFilePath = storagePath + "InputFile" + numeroTramite + numeroRadicado + idUsuarioFirmante + ".pdf";
        String newInputFilePath = storagePath + "NewInputFile" + numeroTramite + numeroRadicado + idUsuarioFirmante
                + ".pdf";
        String outputFilePath = storagePath + "OutputFile" + numeroTramite + numeroRadicado + idUsuarioFirmante
                + ".pdf";
        String ImagenFilePath = storagePath + "ImagenFirmaFile" + numeroTramite + numeroRadicado + idUsuarioFirmante
                + ".jpg";

        base64toFile(documentoPDF, inputFilePath, documentoPdf2);
        base64toFile(ImagenJPG, ImagenFilePath, firmaImagen);
        Integer[] EndPosition = FirmaPdfPosicionFinDocumento.getEndTextPosition(inputFilePath, null);
        Integer espacioLinea = 12;
        Integer lineasAdicionales = 7;
        Integer largo = 150;
        Integer ancho = 80;
        Integer posX = EndPosition[0];
        Integer posY = ((EndPosition[1] - espacioLinea) - ancho);

        if ((posY - (espacioLinea * lineasAdicionales)) < 0) {
            FirmaPdfUtilidadesBasicas.agregarPaginaPDF(inputFilePath, newInputFilePath);
            inputFilePath = newInputFilePath;
            posY = ((800 - espacioLinea) - ancho);
        }

        OutputStream fos = new FileOutputStream(new File(outputFilePath));

        PdfReader pdfReader = new PdfReader(inputFilePath);
        PdfStamper pdfStamper = new PdfStamper(pdfReader, fos);

        for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
            if (i == pdfReader.getNumberOfPages()) {
                PdfContentByte pdfContentByte = pdfStamper.getUnderContent(i);
                Image image = Image.getInstance(ImagenFilePath);
                image.scaleAbsolute(largo, ancho);
                image.setAbsolutePosition(posX + 50, posY);
                pdfContentByte.addImage(image);
                pdfContentByte.beginText();
                pdfContentByte.setFontAndSize(
                        BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1257, BaseFont.EMBEDDED), 12); // set font
                // and size
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("______________________________________");
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("Nombre: " + firmanteNombre);
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("Tipo de Identificación: " + firmanteTipoDocumento);
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("Número de Identificación: " + firmanteNumeroDocumento);
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("Teléfono: " + firmanteTelefono);
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("Correo: " + firmanteCorreo);
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                pdfContentByte.showText("Calidad en que Firma: " + firmanteRool);
                posY = posY - espacioLinea;
                pdfContentByte.setTextMatrix(posX, posY);
                SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy HH:mm");// dd/MM/yyyy
                Date now = new Date();
                String strDate = sdfDate.format(now);
                pdfContentByte.showText("Fecha y Hora: " + strDate);
            }
        }
        pdfStamper.close();
        pdfStamper.flush();
        pdfReader.close();
        fos.close();
        fos.flush();

        String documentoPDFFirmado = filetoBase64(outputFilePath);

        borrarFile(inputFilePath);
        borrarFile(oldinputFilePath);
        borrarFile(newInputFilePath);
        borrarFile(outputFilePath);
        borrarFile(ImagenFilePath);

        return documentoPDFFirmado;

    }

    public static void base64toFile(String code64, String outputFile, String contennido) {
        File file = new File(outputFile);

        try (FileOutputStream fos = new FileOutputStream(file);) {
            byte[] decoder = Base64.getDecoder().decode(contennido);
            fos.write(decoder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String filetoBase64(String inputFile) {
        String base64File = "";
        File file = new File(inputFile);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a file from file system
            byte fileData[] = new byte[(int) file.length()];
            imageInFile.read(fileData);
            base64File = Base64.getEncoder().encodeToString(fileData);
        } catch (FileNotFoundException e) {
            System.err.println("No Existe Archivo" + e);
        } catch (IOException ioe) {
            System.err.println("No puede Leer el Archivo " + ioe);
        }
        return base64File;
    }

    public static void borrarFile(String deleteFile) {
        File file = new File(deleteFile);
        try {
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.err.println("Exception occurred");
        }
    }

    public static void deleteFile(String deleteFile) {
        Path path = Paths.get(deleteFile);
        try {

            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
