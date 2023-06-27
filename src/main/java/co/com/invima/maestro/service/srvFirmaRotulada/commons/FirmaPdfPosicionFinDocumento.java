package co.com.invima.maestro.service.srvFirmaRotulada.commons;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.io.IOException;

public class FirmaPdfPosicionFinDocumento {

    public FirmaPdfPosicionFinDocumento() {
        // TODO Auto-generated constructor stub
    }

    public static Integer[] getEndTextPosition(String filePath, Integer pageNum) throws IOException {
        final Integer[] result = new Integer[2];
        PdfReader pdfReader = new PdfReader(filePath);
        if (null == pageNum) {
            pageNum = pdfReader.getNumberOfPages();
        }
        new PdfReaderContentParser(pdfReader).processContent(pageNum, new RenderListener() {
            public void beginTextBlock() {

            }
            float xMin = 800;
            public void renderText(TextRenderInfo textRenderInfo) {
                String text = textRenderInfo.getText();
                Float textFloat = textRenderInfo.getBaseline().getBoundingRectange();
                float x = textFloat.x;
                if (x < xMin) {
                    xMin = x;
                }
                float y = textFloat.y;
                result[0] = (int) xMin;
                result[1] = (int) y;
            }

            public void endTextBlock() {

            }

            public void renderImage(ImageRenderInfo renderInfo) {

            }
        });
        pdfReader.close();
        return result;
    }
}
