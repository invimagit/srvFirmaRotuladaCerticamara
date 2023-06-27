package co.com.invima.maestro.service.srvFirmaRotulada.commons.converter;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirmaRotuladaConverter {

    private final ModelMapper modelMapper;

    @Autowired
    public FirmaRotuladaConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * @param empresaDAO
     * @return empresaDTO
     * @author JBermeo
     * method to convert EmpresaDAO to EmpresaDTO
     */
   /* public EmpresaDTO empresaDAOtoDTO(EmpresaDAO empresaDAO, ModelMapper modelMapper) {
        EmpresaDTO empresaDTO = new EmpresaDTO();
        modelMapper.map(empresaDAO, empresaDTO);
        return empresaDTO;

    }*/


    /**
     * @param empresaDTO
     * @return empresaDAO
     * @author JBermeo
     * method to convert EmpresaDTO to EmpresaDAO
     */
    /*public EmpresaDAO empresaDTOtoDAO(EmpresaDTO empresaDTO,
                                      ModelMapper modelMapper) throws NotFoundException {
        EmpresaDAO empresaDAO = new EmpresaDAO();
        modelMapper.map(empresaDTO, empresaDAO);

        return empresaDAO;
    }*/

}
