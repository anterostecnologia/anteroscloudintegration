package br.com.anteros.cloud.integration.filesharing.resource;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import br.com.anteros.cloud.integration.exception.AnterosCloudIntegrationServerException;
import br.com.anteros.cloud.integration.filesharing.CloudResultInfo;
import br.com.anteros.cloud.integration.filesharing.CloudFile;
import br.com.anteros.cloud.integration.filesharing.impl.WebServerFileManager;

/**
 * Resource que permite salvar arquivos para serem compartilhados via webserver.
 * 
 * @author Edson Martins - Relevant Solutions
 *
 */
@Controller
@RequestMapping(value = "/webServerIntegration")
public class WebServerFileResource { 
	

	@Autowired(required = false)
	private WebServerFileManager webServerFileManager;

	/**
	 * Permite enviar um arquivo para o webserver e compartilhar
	 * @param folderName Nome da pasta
	 * @param name Nome do arquivo
	 * @param file Arquivo
	 * @return Info do link
	 */
	@RequestMapping(value = "/uploadAndShareFile", method = RequestMethod.POST)
	public @ResponseBody CloudResultInfo uploadAndShareFile(
			@RequestParam(name = "folder", required = false) String folderName,
			@RequestParam(name = "name", required = false) String name, @RequestParam("file") MultipartFile file) {

		Tika tika = new Tika();
		String mimeType = "";
		try {
			mimeType = tika.detect(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return webServerFileManager.uploadAndShareFile(folderName, name, file.getBytes(), mimeType);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}

	}

	/**
	 * Permite criar uma pasta
	 * 
	 * @param folderName Nome da pasta
	 */
	@RequestMapping(value = "/createFolder", method = RequestMethod.POST)
	public @ResponseBody void createFolder(@RequestParam(name = "folder", required = true) String folderName) {
		try {
			webServerFileManager.createFolder(folderName);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}

	/**
	 * Permite remover uma pasta
	 * 
	 * @param folderName
	 */
	@RequestMapping(value = "/deleteFolder", method = RequestMethod.POST)
	public @ResponseBody void deleteFolder(@RequestParam(name = "folder", required = true) String folderName) {
		try {
			webServerFileManager.removeFolder(folderName);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}

	/**
	 * Retorna uma lista de todos os arquivos compartilhados
	 * 
	 * @param folderName Nome da pasta
	 * @return Lista de arquivos
	 */
	@RequestMapping(value = "/listFiles", method = RequestMethod.POST)
	public @ResponseBody Collection<CloudFile> listFiles(
			@RequestParam(name = "folder", required = false) String folderName) {
		try {
			return webServerFileManager.listFiles(folderName);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}

	/**
	 * Retorna uma lista de imagens compartilhadas
	 * 
	 * @param folderName Nome da pasta
	 * @return Lista de imagens
	 */
	@RequestMapping(value = "/listImages", method = RequestMethod.POST)
	public @ResponseBody Collection<CloudFile> listImages(
			@RequestParam(name = "folder", required = false) String folderName) {
		try {
			return webServerFileManager.listImages(folderName);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}

	/**
	 * Permite enviar vários arquivos para o webserver e compartilhar
	 * @param folder Nome da pasta
	 * @param names Nomes dos arquivos
	 * @param files Arquivos
	 * @return Lista de resultados do compartilhamento
	 */
	@RequestMapping(value = "/uploadMultipleFiles", method = RequestMethod.POST)
	public @ResponseBody Collection<CloudResultInfo> uploadMultipleFiles(
			@RequestParam(name = "folder", required = false) String folder,
			@RequestParam(name = "names", required = true) String[] names,
			@RequestParam("file") MultipartFile[] files) {
		Collection<CloudResultInfo> result = new ArrayList<CloudResultInfo>();
		int index = 0;
		try {
			for (MultipartFile file : files) {
				Tika tika = new Tika();
				String mimeType = "";
				try {
					mimeType = tika.detect(file.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				result.add(webServerFileManager.uploadAndShareFile(folder, names[index], file.getBytes(), mimeType));
				index++;
			}
			return result;
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}
	

	/**
	 * Permite criar um preview de um arquivo no webserver e compartilhar
	 * 
	 * @param content Conteúdo 
	 * @return Link de acesso do preview
	 * @throws Exception
	 */
	@RequestMapping(value = "/previewWebPage", method = RequestMethod.POST)
	public @ResponseBody String generatePreviewWebPage(@RequestBody String content) throws Exception {
		String fileName = UUID.randomUUID().toString()+".html";
		File fld = new File(webServerFileManager.getUploadFolder() + File.separator + "preview");
		File arquivo = new File(fld, fileName);		
		FileOutputStream fos = new FileOutputStream(arquivo);
		fos.write(content.getBytes("UTF-8"));
		fos.flush();
		fos.close();
		arquivo.setReadable(true, false);
		return webServerFileManager.getUrlSite()+"/uploads/preview/"+fileName;
	}

}