package co.com.invima.maestro.service.srvFirmaRotulada.service;


import co.com.invima.maestro.modeloTransversal.dto.generic.GenericResponseDTO;

import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;

public interface IFirmaRotuladaService {

    ResponseEntity<GenericResponseDTO> firmaDigital(String genericRequestDTO, String firmaImagen, String documentoPdf);

    ResponseEntity<GenericResponseDTO> firmaCertificada( String json) throws ParseException;
    
}
