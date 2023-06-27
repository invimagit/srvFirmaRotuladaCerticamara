package co.com.invima.maestro.service.srvFirmaRotulada.web.api.rest.v1;

import co.com.invima.maestro.modeloTransversal.dto.generic.GenericResponseDTO;
import co.com.invima.maestro.service.srvFirmaRotulada.service.IFirmaRotuladaService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/FirmaRotulada")
@CrossOrigin({"*"})
public class FirmaRotuladaController implements IFirmaRotuladaController {

    private final IFirmaRotuladaService service;

    @Autowired
    public FirmaRotuladaController(IFirmaRotuladaService service) {
        this.service = service;
    }

    @Override
    @PostMapping(value = "/firmaDigital")
    @ApiOperation(value = "consulta las empresas extranjeras", notes = "consulta las empresas extranjeras")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. El recurso se obtiene correctamente", response = GenericResponseDTO.class),
            @ApiResponse(code = 400, message = "Bad Request.Esta vez cambiamos el tipo de dato de la respuesta (String)", response = String.class),
            @ApiResponse(code = 500, message = "Error inesperado del sistema")})
    public ResponseEntity<GenericResponseDTO> firmaDigital(@ApiParam(value = "Los parametros para la firma digital estan contenidos en un MultiValueMap<String, Object> y son los siguientes:" +
            "<br> documentoPdf: documento pdf codificado en Base64,\n" +
            "<br> firmaImagen: imagen codificado en Base64 que contiene la firma para el documento,\n" +
            "<br> informacion: tipo JSon debe tener la siguiente estructura,\n" +
            "<br> {" +
            "\"documentoPDFBase64\":\" \",\n" +
            "\"imagenJPGBase64\":\" \",\n" +
            "\"numeroTramite\": ,\n" +
            "\"numeroRadicado\": ,\n" +
            "\"idUsuarioFirmante\": ,\n" +
            "\"firmanteNombre\":\" \",\n" +
            "\"firmanteTipoDocumento\":\" \",\n" +
            "\"firmanteNumeroDocumento\":\" \",\n" +
            "\"firmanteTelefono\":\" \",\n" +
            "\"firmanteCorreo\":\" \",\n" +
            "\"firmanteRol\":\"\"\n" +
            "}" +
            "<br>") @RequestParam MultiValueMap<String, Object> firmaRotuladaParams) {
        String documentoPdf = String.valueOf(firmaRotuladaParams.get("documentoPdf")!=null?firmaRotuladaParams.get("documentoPdf").get(0):"");
        String firmaImagen = String.valueOf(firmaRotuladaParams.get("firmaImagen")!=null?firmaRotuladaParams.get("firmaImagen").get(0):"");
        String genericRequestDTO = String.valueOf(firmaRotuladaParams.get("informacion")!=null?firmaRotuladaParams.get("informacion").get(0):"");
        return service.firmaDigital(genericRequestDTO, firmaImagen, documentoPdf);
    }

    @Override
    @PostMapping(value = "/firmaCertificada")
    @ApiOperation(value = "Firmar Cetificada con Certicamara Archivos PDF ", notes = "Firmar Cetificada con Certicamara Archivos PDF ")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. El recurso se obtiene correctamente", response = GenericResponseDTO.class),
            @ApiResponse(code = 400, message = "Bad Request.Esta vez cambiamos el tipo de dato de la respuesta (String)", response = String.class),
            @ApiResponse(code = 500, message = "Error inesperado del sistema")})
    public ResponseEntity<GenericResponseDTO> firmaCertificada(@ApiParam(value =
            "el parametro empresa debe ser un json con la siguiente estructura:" +
                    "<br>" +
                    "{\r\n"
                    + "  \"objAuditoria\": {\r\n"
                    + "    \"usuario\": \"tramitesgestor22@gmail.com\",\r\n"
                    + "    \"ip\": \" 181.49.173.42\"\r\n"
                    + "  },\r\n"
                    + "  \"objOperacion\": {\r\n"
                    + "    \"pdfFileBase64\": \"{Coloque el PDF Codificado Base64}\""
                    + "    \"idTramite\": \"41160\",\r\n"
                    + "    \"idFuncionarioFirmante\": \"15\"\r\n"
                    + "    \"idFuncionarioProyector\": \"\"\r\n"
                    + "    \"idFuncionarioRevisor\": \"\"\r\n"
                    + "  }\r\n"
                    + "}" +
                    "<br>", required = true) String json) throws ParseException {
        return service.firmaCertificada(json);
    }
}
