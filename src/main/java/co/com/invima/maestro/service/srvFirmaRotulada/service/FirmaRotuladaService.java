package co.com.invima.maestro.service.srvFirmaRotulada.service;

import co.com.invima.maestro.modeloTransversal.dto.generic.GenericResponseDTO;
import co.com.invima.maestro.service.srvFirmaRotulada.commons.Utils;
import co.com.invima.maestro.service.srvFirmaRotulada.commons.FirmaPDF;
import co.com.invima.maestro.service.srvFirmaRotulada.commons.converter.FirmaRotuladaConverter;
import co.com.invima.maestro.service.srvFirmaRotulada.repository.IFirmaRotuladaRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.IDN;
import java.net.URI;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import co.com.certicamara.certitoken.CertiTokenConfiguration;
import co.com.certicamara.commons.ProcessResponseSign;
import co.com.certicamara.listCertificate.CertificateClient;
import co.com.certicamara.listCertificate.model.Certificate;
import co.com.certicamara.listCertificate.model.EntityRequest;
import co.com.certicamara.listCertificate.model.EntityResponse;
import co.com.certicamara.sign.Sign;
import co.com.certicamara.sign.SignFactory;
import co.com.certicamara.sign.SignatureParameters;
import co.com.certicamara.sign.certificate.CertificateConfiguration;
import co.com.certicamara.sign.certificate.CertificateFromCertiToken;
import co.com.certicamara.sign.exception.CertificateInitException;
import co.com.certicamara.sign.exception.SignException;
import co.com.certicamara.sign.pdf.PdfParameters;
import co.com.certicamara.sign.utils.UtilsSign;
import co.com.certicamara.verify.certificates.revocation.RevocationVerify;
import co.com.certicamara.verify.certificates.revocation.VerifyType;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirmaRotuladaService implements IFirmaRotuladaService {

	private final ModelMapper modelMapper;

	private final IFirmaRotuladaRepository iFirmaRotuladaRepository;

	private final FirmaRotuladaConverter firmaRotuladaConverter;

	private String driver = "spring.datasource.driver-class-name";
	private String username = "spring.datasource.username";
	private String password = "spring.datasource.password";
	private String url = "spring.datasource.url";
	private String schema = "spring.jpa.properties.hibernate.default_schema";
	private String stringout = "string_OUT";
	private String jsonout = "json_OUT";
	private String jsonin = "json_IN";

	@Autowired
	public FirmaRotuladaService(FirmaRotuladaConverter firmaRotuladaConverter, ModelMapper modelMapper,
			IFirmaRotuladaRepository iFirmaRotuladaRepository) {

		this.firmaRotuladaConverter = firmaRotuladaConverter;
		this.modelMapper = modelMapper;
		this.iFirmaRotuladaRepository = iFirmaRotuladaRepository;
	}

	@Autowired
	public Environment env;

	@Override
	public ResponseEntity<GenericResponseDTO> firmaCertificada(String pJson) throws ParseException {
		JSONParser parse = new JSONParser();
		JSONObject respuestTramiteObj = null;
		JSONObject respuestFuncionarioObj = null;
		JSONObject respuestProyectorObj = null;
		JSONObject respuestRevisorObj = null;

		try {
			JSONObject json_in = (JSONObject) parse.parse(pJson);
			JSONObject objAuditoriaIn = (JSONObject) json_in.get("objAuditoria");
			String usuario = (String) objAuditoriaIn.get("usuario");
			String ip = (String) objAuditoriaIn.get("ip");
			JSONObject objOperacionIn = (JSONObject) json_in.get("objOperacion");
			String pdfFileBase64 = (String) objOperacionIn.get("pdfFileBase64");
			String IdTramite = (String) objOperacionIn.get("idTramite");
			String IdDocumento = (String) objOperacionIn.get("idDocumento");
			String IdFuncionarioFirmante = (String) objOperacionIn.get("idFuncionarioFirmante");
			String idFuncionarioProyector = (String) objOperacionIn.get("idFuncionarioProyector");
			String idFuncionarioRevisor = (String) objOperacionIn.get("idFuncionarioRevisor");
			String pathPDFfile = env.getProperty("pathPDF") + "/Firmar-Tramite-" + IdTramite + ".pdf";
			Utils.base64toFile(pathPDFfile, pdfFileBase64);

			JSONObject objAuditoria_out = new JSONObject();
			JSONObject objOperacion_out = new JSONObject();
			JSONObject json_out = new JSONObject();
			objAuditoria_out.put("usuario", usuario);
			objAuditoria_out.put("ip", ip);
			if (!Utils.isnull(IdTramite)) {
				objOperacion_out.put("idTramite", IdTramite);
			}
			if (!Utils.isnull(IdDocumento)) {
				objOperacion_out.put("idDocumento", IdDocumento);
			}
			json_out.put("objAuditoria", objAuditoria_out);
			json_out.put("objOperacion", objOperacion_out);
			String pJson_out = (String) json_out.toJSONString();
			log.info(" << pJson_out >> " + pJson_out);

			String procedimiento = "dbo.USP_FirmaCertificadaTramite(?,?)";
			Utils.cargarDriver(env.getProperty(driver));
			String informacionTramite;
			try (Connection conn = Utils.realizarConexion(env.getProperty(username), env.getProperty(password),
					env.getProperty(url), env.getProperty(schema));
					CallableStatement cStmt = conn.prepareCall("{call " + procedimiento + "}")) {
				String datoEntrada = jsonin;
				cStmt.setString(datoEntrada, pJson_out);
				cStmt.registerOutParameter(jsonout, Types.LONGVARCHAR);
				cStmt.execute();
				informacionTramite = cStmt.getString(jsonout);
			}
			JSONObject jsonResponse = (JSONObject) parse.parse(informacionTramite);
			log.info(" << jsonResponse >> " + jsonResponse.toJSONString());

			JSONObject respuestGenerica = (JSONObject) jsonResponse.get("respuesta");
			JSONArray respuestMensaje = (JSONArray) jsonResponse.get("mensaje");
			String codigo = (String) respuestGenerica.get("codigo");
			
			if (codigo.equals("00")) {
				respuestTramiteObj = (JSONObject) respuestMensaje.get(0);
			}
			JSONObject objAuditoria_out_fun = new JSONObject();
			JSONObject objOperacion_out_fun = new JSONObject();
			JSONObject json_out_fun = new JSONObject();
			objAuditoria_out_fun.put("usuario", usuario);
			objAuditoria_out_fun.put("ip", ip);
			objOperacion_out_fun.put("idFuncionario", IdFuncionarioFirmante);
			json_out_fun.put("objAuditoria", objAuditoria_out_fun);
			json_out_fun.put("objOperacion", objOperacion_out_fun);
			String pJson_out_fun = (String) json_out_fun.toJSONString();
			log.info(" << pJson_out_fun >> " + pJson_out_fun);

			String procedimiento_fun = "dbo.USP_FirmaCertificadaFuncionario(?,?)";
			Utils.cargarDriver(env.getProperty(driver));
			String firmaCertificadaFuncionario;
			try (Connection conn = Utils.realizarConexion(env.getProperty(username), env.getProperty(password),
					env.getProperty(url), env.getProperty(schema));
					CallableStatement cStmt = conn.prepareCall("{call " + procedimiento_fun + "}")) {
				String datoEntrada = jsonin;
				cStmt.setString(datoEntrada, pJson_out_fun);
				cStmt.registerOutParameter(jsonout, Types.LONGVARCHAR);
				cStmt.execute();
				firmaCertificadaFuncionario = cStmt.getString(jsonout);
			}
			JSONObject jsonResponse_fun = (JSONObject) parse.parse(firmaCertificadaFuncionario);

			JSONObject respuestGenerica_fun = (JSONObject) jsonResponse_fun.get("respuesta");
			JSONArray respuestMensaje_fun = (JSONArray) jsonResponse_fun.get("mensaje");
			String codigo_fun = (String) respuestGenerica_fun.get("codigo");
			log.info(" << jsonResponse_fun >> " + jsonResponse_fun);

			if (codigo_fun.equals("00")) {
				respuestFuncionarioObj = (JSONObject) respuestMensaje_fun.get(0);
				String NombrePersona = (String) respuestFuncionarioObj.get("NombrePersona");
				String NombreDireccion = (String) respuestFuncionarioObj.get("NombreDireccion");
				String NombreDependencia = (String) respuestFuncionarioObj.get("NombreDependencia");
				String RolPersona = (String) respuestFuncionarioObj.get("RolPersona");
				String EnCalidadDe = RolPersona.trim() + " DE LA " + NombreDireccion.trim();
				EnCalidadDe = EnCalidadDe.toUpperCase();
				String Profesion = (String) respuestFuncionarioObj.get("Profesion");
				String Cargo = (String) respuestFuncionarioObj.get("Cargo");
				String Base64Firma = (String) respuestFuncionarioObj.get("Base64Firma");
				String CerticamaraNuIp = (String) respuestFuncionarioObj.get("CerticamaraNuIp");
				String CerticamaraPassword = (String) respuestFuncionarioObj.get("CerticamaraPassword");
				if (idFuncionarioProyector.length() > 0 || !idFuncionarioProyector.isEmpty()) {
					JSONObject proAuditoria_out = new JSONObject();
					JSONObject proOperacion_out = new JSONObject();
					JSONObject json_pro = new JSONObject();
					proAuditoria_out.put("usuario", usuario);
					proAuditoria_out.put("ip", ip);
					proOperacion_out.put("idFuncionario", idFuncionarioProyector);
					json_pro.put("objAuditoria", objAuditoria_out);
					json_pro.put("objOperacion", proOperacion_out);
					String pJson_pro = (String) json_pro.toJSONString();
					log.info(" << pJson_pro >> " + pJson_pro);

					Utils.cargarDriver(env.getProperty(driver));
					String FuncionarioProyector;
					try (Connection conn = Utils.realizarConexion(env.getProperty(username), env.getProperty(password),
							env.getProperty(url), env.getProperty(schema));
							CallableStatement cStmt = conn.prepareCall("{call " + procedimiento + "}")) {
						String datoEntrada = jsonin;
						cStmt.setString(datoEntrada, pJson_pro);
						cStmt.registerOutParameter(jsonout, Types.LONGVARCHAR);
						cStmt.execute();
						FuncionarioProyector = cStmt.getString(jsonout);
					}
					JSONObject jsonResponsePro = (JSONObject) parse.parse(FuncionarioProyector);

					JSONObject respuestGenericaPro = (JSONObject) jsonResponsePro.get("respuesta");
					JSONArray respuestMensajePro = (JSONArray) jsonResponsePro.get("mensaje");
					String codigoPro = (String) respuestGenericaPro.get("codigo");
					if (codigoPro.equals("00")) {
						respuestProyectorObj = (JSONObject) respuestMensajePro.get(0);
						String NombreProyector = (String) respuestProyectorObj.get("NombrePersona");
						log.info(" << NombreProyector >> " + NombreProyector);
					}
				}
				if (idFuncionarioRevisor.length() > 0 || !idFuncionarioRevisor.isEmpty()) {
					JSONObject revAuditoria_out = new JSONObject();
					JSONObject revOperacion_out = new JSONObject();
					JSONObject json_rev = new JSONObject();
					revAuditoria_out.put("usuario", usuario);
					revAuditoria_out.put("ip", ip);
					revOperacion_out.put("idFuncionario", idFuncionarioRevisor);
					json_rev.put("objAuditoria", revAuditoria_out);
					json_rev.put("objOperacion", revOperacion_out);
					String pJson_rev = (String) json_rev.toJSONString();
					log.info(" << pJson_rev >> " + pJson_rev);

					Utils.cargarDriver(env.getProperty(driver));
					String FuncionarioRevisor;
					try (Connection conn = Utils.realizarConexion(env.getProperty(username), env.getProperty(password),
							env.getProperty(url), env.getProperty(schema));
							CallableStatement cStmt = conn.prepareCall("{call " + procedimiento + "}")) {
						String datoEntrada = jsonin;
						cStmt.setString(datoEntrada, pJson_rev);
						cStmt.registerOutParameter(jsonout, Types.LONGVARCHAR);
						cStmt.execute();
						FuncionarioRevisor = cStmt.getString(jsonout);
					}
					JSONObject jsonResponseRev = (JSONObject) parse.parse(FuncionarioRevisor);

					JSONObject respuestGenericaRev = (JSONObject) jsonResponseRev.get("respuesta");
					JSONArray respuestMensajeRev = (JSONArray) jsonResponseRev.get("mensaje");
					String codigoRev = (String) respuestGenericaRev.get("codigo");
					if (codigoRev.equals("00")) {
						respuestRevisorObj = (JSONObject) respuestMensajeRev.get(0);
						String NombreRevisor = (String) respuestRevisorObj.get("NombrePersona");
						log.info(" << NombreRevisor >> " + NombreRevisor);
					}
				}
				if (!Utils.isnull(CerticamaraNuIp) && !Utils.isnull(CerticamaraPassword)) {
					log.info(" << CerticamaraNuIp >> " + CerticamaraNuIp);
					log.info(" << CerticamaraPassword >> " + CerticamaraPassword);
					log.info(" << NombrePersona >> " + NombrePersona);
					log.info(" << NombreDireccion >> " + NombreDireccion);
					Certificate certificate = this.obtenerCertificado(CerticamaraNuIp, CerticamaraPassword);
					if (!Utils.isnull(certificate)) {
						log.info(" << certificate.getCn() >> " + certificate.getCn());
						String rutaCRL = env.getProperty("rutaCRL");
						String keystoresPath = env.getProperty("keystoresPath");
						String signReason = env.getProperty("signReason");
						String signLocation = env.getProperty("signLocation");
						String signFieldName = env.getProperty("signFieldName");
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
						String contentSignature = "Firmado digitalmente por " + certificate.getCn();
						contentSignature += "\nFecha: " + sdf.format(new Date());
						contentSignature += "\nRazón: " + signReason + "\nLocación: " + signLocation;

						/************** CONFIGURACIONES ESTAMPADO CRONOLÓGICO *******************/
						String rutaTsasCertificates = "./TSAs"; // PRODUCCION
						String tsaURL = env.getProperty("tsaURL");
						String tsaPolicyOID = env.getProperty("tsaPolicyOID");
						String stamperUser = env.getProperty("stamperUser");
						String stamperPassword = env.getProperty("stamperPassword");

						int lowerLeftX = 100;
						int lowerLeftY = 100;
						int upperRightX = 200;
						int upperRightY = 195;
						PdfReader pdfreader = new PdfReader(pathPDFfile);
						int signPage = pdfreader.getNumberOfPages() == 0 ? 1 : pdfreader.getNumberOfPages();
			            RevocationVerify revocationVerify = new RevocationVerify(
			                    VerifyType.CRL_OCSP, null,
			                    rutaCRL, null);
			            revocationVerify.setKeyStorePath(keystoresPath);
						CertificateConfiguration cert = new CertificateFromCertiToken(CerticamaraNuIp,
								CerticamaraPassword, certificate.getSerial(), certificate.getIssuer());
						CertiTokenConfiguration certiTokenConf = new CertiTokenConfiguration(
								env.getProperty("url.sign"), env.getProperty("url.list"));
						// fechaFirma
						GregorianCalendar fecha = new GregorianCalendar();
						String ls_fechafirma = (new StringBuilder(String.valueOf(String.valueOf(fecha.get(1)))))
								.append("/").append(String.valueOf(fecha.get(2) + 1)).append("/")
								.append(String.valueOf(fecha.get(5))).append(" ").append(String.valueOf(fecha.get(11)))
								.append(":").append(String.valueOf(fecha.get(12))).append(":")
								.append(String.valueOf(fecha.get(13))).append(":").append(String.valueOf(fecha.get(14)))
								.toString();
						//
						String meta = fmeta(signFieldName, signPage, respuestTramiteObj, respuestFuncionarioObj, respuestProyectorObj, respuestRevisorObj);
						ArrayList<SignatureParameters> lista = new ArrayList<SignatureParameters>();
						for (int i = 0; i < 1; i++) {
							PdfParameters signParameters = new PdfParameters(
									UtilsSign.getBytesFromFile(prop.ls_archivo), revocationVerify, cert,
									certiTokenConf);
							signParameters.setInformation(signReason, signLocation);
							HashMap<String, String> apostilla = new HashMap<String, String>();
							apostilla.put("Apostilla", meta);
							signParameters.setPersonalProperties(apostilla);
							// Lista de imagenes
							List<ImageSettings> imageSettingList = new LinkedList<ImageSettings>();
							imageSettingList.add(new ImageSettings(signFieldName, null, // UtilsSign.getBytesFromFile(pdf2SignImagePath),
									new Rectangle(lowerLeftX, lowerLeftY, upperRightX, upperRightY), signPage, false,
									contentSignature, PdfSignatureAppearance.RenderingMode.DESCRIPTION));
							signParameters.setImageSettings(imageSettingList);
							signParameters.setVerifyDocument(true);
							signParameters.setCertiToken(true);

							if (isStamped) {
								signParameters.setTimeStampSettings(
										setEstampa(stamperUser, stamperPassword, tsaURL, tsaPolicyOID)); // Estampado
																											// corta
																											// duración
								if (isDoubleStamped) {
									signParameters.setLtv(rutaTsasCertificates,
											setEstampa(stamperUser, stamperPassword, tsaURL, tsaPolicyOID),
											revocationVerify); // Estampado (doble) larga duración
								}
							}

							lista.add(signParameters);
						}
						Sign s = SignFactory.getSigner(SignFactory.PDFCertiToken, lista);
						List<ProcessResponseSign> listaR = s.signData();
						System.out.println("-- started " + Calendar.getInstance().getTime());
						int i = -1;
						for (ProcessResponseSign pp : listaR) {
							i++;
							System.out.println(String.valueOf(pp.isExito()));
							if (pp.isExito()) {
								b = "1";
								System.out.println(prop.ls_destino);
								UtilsSign.saveSignedFile(prop.ls_destino, pp.getResultado());

							} else {
								for (MessageResponse mm : pp.getMessageResponse()) {
									System.out.println(mm.getCodigo() + " " + mm.getMensaje());

								}
							}
						}
					}
				}
				return new ResponseEntity<>(
						GenericResponseDTO.builder()
								.message("Aplico la Firma Certificado de (" + NombrePersona + ") como (" + EnCalidadDe
										+ ") al Documento PDF y Agrego los Datos de Apostilla")
								.objectResponse(pdfFileBase64).statusCode(HttpStatus.OK.value()).build(),
						HttpStatus.OK);
			} else {

				String descripcion = (String) respuestGenerica.get("mensaje");
				return new ResponseEntity<>(GenericResponseDTO.builder().message(descripcion)
						.objectResponse(new JSONObject()).statusCode(HttpStatus.CONFLICT.value()).build(),
						HttpStatus.CONFLICT);

			}

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(
					GenericResponseDTO.builder()
							.message("Error Aplicando la Firma Certificada al Documento PDF " + e.getMessage())
							.objectResponse(false).statusCode(HttpStatus.BAD_REQUEST.value()).build(),
					HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<GenericResponseDTO> firmaDigital(String genericRequestDTO, String firmaImagen,
			String documentoPdf) {
		// TODO Auto-generated method stub
		return null;
	}

	private Certificate obtenerCertificado(String nuip, String password) {
		Certificate certificate = null;
		try {
			log.info(" >>> nuip >>> " + nuip);
			log.info(" >>> password >>> " + password);
			CertificateClient obtenerCertificados = new CertificateClient();
			EntityRequest request = new EntityRequest();
			request.setNuip(nuip);
			request.setPassword(password);
			CertiTokenConfiguration certiTokenConf = new CertiTokenConfiguration("", env.getProperty("url.list"));
			log.info(" >>> env.getProperty(\"url.list\") >>> " + env.getProperty("url.list"));
			log.info(" >>> request.toString() >>> " + request.toString());
			log.info(" >>> certiTokenConf.toString() >>> " + certiTokenConf.toString());
			EntityResponse respuesta = obtenerCertificados.getListCertificates(request, certiTokenConf);
			log.info(" >>> respuesta >>> " + respuesta.toString());
			if (!respuesta.isSuccess()) {
				System.out.println(respuesta.getErrorResponse().getFaultCode());
				System.out.println(respuesta.getErrorResponse().getErrorMessage());
				System.out.println(respuesta.getErrorResponse().getFaultCode());
				System.out.println(respuesta.getErrorResponse().getFaultActor());
				System.out.println(respuesta.getErrorResponse().getException());
				System.out.println(respuesta.getErrorResponse().getErrorMessage());
			} else {
				if (respuesta.getEntity().code == 200) {
					// certificate = respuesta.getEntity().getCertificates().get(0);
					certificate = respuesta.getEntity().getCertificates()
							.get(respuesta.getEntity().getCertificates().size() - 1);
					System.out.println("Certificado obtenido");
					return certificate;
				}
			}
			return certificate;
		} catch (SignException e) {
			log.info("Error al obtener consulta de certificados", e);
			return certificate;
		}
	}

	public String fmeta(String campofirma, int numhojas, JSONObject TramiteObj, JSONObject FuncionarioObj, JSONObject ProyectorObj, JSONObject respuestRevisorObj) {
        String cad = new String("");
        int pos = 0;
        try {
            StringBuffer result = new StringBuffer();
            result.append("<Apostilla>\n");

            cad = env.getProperty("RazonSocialOrganizacion");
            result.append((new StringBuilder("<RazonSocialOrganizacion>")).append(cad).append("</RazonSocialOrganizacion>\n").toString());

            cad = env.getProperty("idEntidad");
            result.append((new StringBuilder("<idEntidad>")).append(cad).append("</idEntidad>").toString());
            
            cad = (String) FuncionarioObj.get("idFuncionarioFirmante");  /* cad = fdato("idautoridad", xmldoc); */
            result.append((new StringBuilder("<idAutoridad>")).append(cad).append("</idAutoridad>\n").toString());
            
            cad = (String) FuncionarioObj.get("Cargo"); /* fdato("cdgcargo", xmldoc);*/
            result.append((new StringBuilder("<codCargo>")).append(cad).append("</codCargo>\n").toString());
            
            cad = (String) TramiteObj.get("idTipoDocumental"); /* fdato("cdgcanc", xmldoc); */
            result.append((new StringBuilder("<codDocumento>")).append(cad).append("</codDocumento>\n").toString());
            
            cad = (String) TramiteObj.get("codigoExpediente"); /* (new StringBuilder(String.valueOf(fdato("cdgdoc", xmldoc)))).append("_").toString(); */
            result.append((new StringBuilder("<prefijoDocumento>")).append(cad).append("</prefijoDocumento>\n").toString());
            
            result.append("<sufijo></sufijo>\n");
            
            cad = (String) TramiteObj.get("numeroRadicado"); /* fdato("nrodocumento", xmldoc); */
            result.append((new StringBuilder("<numDocumento>")).append(cad).toString());
            result.append("</numDocumento>\n");
            
            cad = (String) TramiteObj.get("fechaDocumento"); /* fdato("fchdoc", xmldoc); */
            cad = cad.substring(0, 9).replace("-", "/");
            result.append((new StringBuilder("<fechaDocumento>")).append(cad).toString());
            result.append("</fechaDocumento>\n");
            
            cad = (String) TramiteObj.get("numeroFoliosDocumento");
            result.append((new StringBuilder("<numHojas>")).append(cad).toString());
            result.append("</numHojas>\n");
            
            cad = "";
            result.append((new StringBuilder("<vigenciaDocumento>")).append(cad).toString());
            result.append("</vigenciaDocumento>\n");
            cad = (String) TramiteObj.get("titular"); /*fdato("titular", xmldoc);-*/
            /*if (cad == null || cad.equals("")) {
                cad = fdato("interesado", xmldoc);
            } else {
                pos = cad.toLowerCase().indexOf("con domicilio");
                if (pos > 0) {
                    cad = cad.substring(0, pos);
                }
            }
            if (cad.length() > 50) {
                cad = cad.substring(0, 50);
            }*/
            result.append((new StringBuilder("<nomTitularDocumento>")).append(cad).toString());
            result.append("</nomTitularDocumento>\n");
            result.append("<numFirmaDigital>1</numFirmaDigital>\n");
            result.append((new StringBuilder("<NomCampoFirmaDigital>")).append(campofirma).append("</NomCampoFirmaDigital>\n").toString());
            result.append("<usuario></usuario>\n");
            result.append("</Apostilla>\n");
            System.out.println(result);
            return result.toString();
        } catch (Exception _ex) {
            return "";
        }
    }
}
