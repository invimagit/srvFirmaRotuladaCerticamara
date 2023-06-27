package co.com.invima.maestro.service.srvFirmaRotulada.web.api.rest.v1;


import co.com.invima.maestro.modeloTransversal.dto.generic.GenericResponseDTO;

import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface IFirmaRotuladaController {
	
    ResponseEntity<GenericResponseDTO> firmaDigital(@RequestParam MultiValueMap<String, Object> firmaRotuladaParams);
    
    ResponseEntity<GenericResponseDTO> firmaCertificada(@RequestBody String json) throws ParseException;
    
}
