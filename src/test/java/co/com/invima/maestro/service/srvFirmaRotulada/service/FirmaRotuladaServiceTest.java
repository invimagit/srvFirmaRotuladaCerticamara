package co.com.invima.maestro.service.srvFirmaRotulada.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import co.com.invima.maestro.service.srvFirmaRotulada.commons.ConfiguradorSpring;
import co.com.invima.maestro.service.srvFirmaRotulada.commons.converter.FirmaRotuladaConverter;
import co.com.invima.maestro.service.srvFirmaRotulada.web.api.rest.v1.FirmaRotuladaController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ConfiguradorSpring.class})
public class FirmaRotuladaServiceTest {

    @Autowired
    FirmaRotuladaController firmaRotuladaController;

    @Autowired
    FirmaRotuladaConverter firmaRotuladaConverter;

    @Autowired
    private ModelMapper modelMapper;

    @Test
    public void testDefault() {
    	 assertEquals(Boolean.TRUE, Boolean.TRUE);
    }

    /*@Test
    public void firmaDigital() {
        String json = "{\n" +
                "\"documentoPDFBase64\":\"C:/InvimaPDF/Documento_para_Firmas_Sin_Label.pdf\",\n" +
                "\"imagenJPGBase64\":\"C:/InvimaPDF/FirmaJHSZ.jpg\",\n" +
                "\"numeroTramite\":1023,\n" +
                "\"numeroRadicado\":20211010,\n" +
                "\"idUsuarioFirmante\":123,\n" +
                "\"firmanteNombre\":\"Juan H. Sanchez\",\n" +
                "\"firmanteTipoDocumento\":\"Cedula Extranjeria\",\n" +
                "\"firmanteNumeroDocumento\":\"910172\",\n" +
                "\"firmanteTelefono\":\"3212324747\",\n" +
                "\"firmanteCorreo\":\"jhsanchez@soaint.com\",\n" +
                "\"firmanteRol\":\"Apoderado\"\n" +
                "}";

        try {

            ResponseEntity<GenericResponseDTO> response = firmaRotuladaController.firmaDigital(json);
            Object respuesta = response.getBody().getObjectResponse();
            System.out.println("LA RESPUESTA" + respuesta);
            //assertEquals(response.getBody().getStatusCode(), 200);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
