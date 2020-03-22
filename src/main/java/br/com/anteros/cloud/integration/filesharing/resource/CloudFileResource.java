package br.com.anteros.cloud.integration.filesharing.resource;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import br.com.anteros.cloud.integration.exception.AnterosCloudIntegrationServerException;
import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.cloud.integration.filesharing.CloudResultInfo;
import br.com.anteros.cloud.integration.filesharing.CloudFile;
import br.com.anteros.cloud.integration.filesharing.CloudShareFolder;

/**
 * Resource que permite salvar arquivos na Nuvem para serem compartilhados.
 * 
 * @author Edson Martins - Relevant Solutions
 *
 */
@Controller
@RequestMapping(value = "/cloudIntegration")
public class CloudFileResource {
	

	@Autowired
	private CloudFileManager cloudFileManager;

	/**
	 * Permite enviar um arquivo para o nuvem e compartilhar
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
			return cloudFileManager.uploadAndShareFile(folderName, name, file.getBytes(), mimeType);
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
			cloudFileManager.createFolder(folderName);
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
			cloudFileManager.removeFolder(folderName);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}
	
	/**
	 * Remove um arquivo
	 * 
	 * @param folderName
	 * @param fileName
	 */
	@RequestMapping(value = "/deleteFile", method = RequestMethod.POST)
	public @ResponseBody void deleteFile(@RequestParam(name = "folder", required = true) String folderName, @RequestParam(name = "name", required = true) String name) {
		try {
			cloudFileManager.removeFile(folderName, name);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}

	/**
	 * Retorna uma lista de todos os pastas compartilhados
	 * 
	 * @param folderName Nome da pasta
	 * @return Lista de pastas
	 */
	@RequestMapping(value = "/listFolders", method = RequestMethod.POST)
	public @ResponseBody Collection<CloudShareFolder> listFolders(
			@RequestParam(name = "folder", required = false) String folderName) {
		try {
			cloudFileManager.createFolder(folderName);
			return cloudFileManager.listFolders(folderName);
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
			return cloudFileManager.listFiles(folderName);
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
			return cloudFileManager.listImages(folderName);
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}

	/**
	 * Permite enviar vários arquivos para a nuvem e compartilhar
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
				result.add(cloudFileManager.uploadAndShareFile(folder, names[index], file.getBytes(), mimeType));
				index++;
			}
			return result;
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}
	
	

}