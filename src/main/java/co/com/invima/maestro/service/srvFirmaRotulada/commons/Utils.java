package co.com.invima.maestro.service.srvFirmaRotulada.commons;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Base64;
import java.util.Properties;

import co.com.invima.canonico.modeloCanonico.dto.generic.GenericResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.multipart.MultipartFile;

import co.com.invima.maestro.service.srvFirmaRotulada.service.FirmaRotuladaService;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

@Slf4j
public class Utils {

	public static String filetoBase64(String inputFile) {
		String base64File = "";
		File file = new File(inputFile);
		try (FileInputStream imageInFile = new FileInputStream(file)) {
			// Reading a file from file system
			byte fileData[] = new byte[(int) file.length()];
			imageInFile.read(fileData);
			base64File = Base64.getEncoder().encodeToString(fileData);
		} catch (FileNotFoundException e) {
			log.info("No Existe Archivo" + e);
		} catch (IOException ioe) {
			log.info("No puede Leer el Archivo " + ioe);
		}
		return base64File;
	}

	public static InputStream base64toFile(String base64File) {
		byte[] decodedBytes = Base64.getDecoder().decode(base64File);
		return new ByteArrayInputStream(decodedBytes);
	}

    public static void base64toFile(String outputFile, String contennido) {
        File file = new File(outputFile);

        try (FileOutputStream fos = new FileOutputStream(file);) {
            byte[] decoder = Base64.getDecoder().decode(contennido);
            fos.write(decoder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static void cargarDriver(String clasName) {
		try {
			Class.forName(clasName);
		} catch (ClassNotFoundException e1) {
			log.error("Error 1: " + e1);
			e1.printStackTrace();
		}
	}

	public static Connection realizarConexion(String user, String password, String url, String schema) {
		Properties connectionProps = new Properties();
		Connection conn;
		try {
			connectionProps.put("user", user);
			connectionProps.put("password", password);
			conn = DriverManager.getConnection(url, connectionProps);
			conn.setSchema(schema);
			return conn;
		} catch (Exception e) {
			log.error("Error 2: " + e);
			e.printStackTrace();
			return null;
		}
	}

	public static Boolean isnull(Object a) {
		Boolean is = false;
		if (a == null) {
			is = true;
		}
		return is;
	}

	public static void borrarFile(String deleteFile) {
		File file = new File(deleteFile);
		try {
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			log.error("Exception occurred");
		}
	}

}
