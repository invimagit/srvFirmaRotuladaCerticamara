package co.com.invima.maestro.service.srvFirmaRotulada.repository;


import co.com.invima.maestro.modeloTransversal.entities.empresa.EmpresaDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

@Repository
public interface IFirmaRotuladaRepository extends JpaRepository<EmpresaDAO, Integer>, JpaSpecificationExecutor<EmpresaDAO> {

    @Procedure("dbo.USP_FirmaCertificadaFuncionario")
    String firmaCertificadaFuncionario(String json);

    @Procedure("dbo.USP_FirmaCertificadaTramite")
    String firmaCertificadaTramite(String json);


}
