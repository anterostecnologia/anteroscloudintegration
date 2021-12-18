package br.com.anteros.cloud.integration.filesharing.resource;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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
import br.com.anteros.core.utils.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 * Resource que permite salvar arquivos na Nuvem para serem compartilhados.
 * 
 * @author Edson Martins - Relevant Solutions
 *
 */
@Controller
@RequestMapping(value = "/cloudIntegration")
public class CloudFileResource {
	

	@Autowired(required = false)
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
			@RequestParam(name = "name", required = false) String name, @RequestParam(value = "file", required = false) MultipartFile file,
			HttpServletRequest request) {

		InputStream is=null;
		if (file == null) {
			try {
				Collection<Part> parts = request.getParts();
				if (parts.size()>0){
					Part part = parts.iterator().next();
					if (part.getName().equals("file")){
						is = part.getInputStream();
					}
				}
			} catch (Exception e) {
			}
		} else {
			try {
				is = file.getInputStream();
			} catch (IOException e) {
				throw new RuntimeException(("Erro lendo arquivo."));
			}
		}

		if (is==null){
			throw new RuntimeException(("Parâmetro file não encontrado."));
		}

		Tika tika = new Tika();
		String mimeType = "";
		try {
			mimeType = tika.detect(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			
			File _file = new File(File.separator+"tmp"+File.separator+UUID.randomUUID().toString()+".tmp");
			
			FileOutputStream fos = new FileOutputStream(_file);				
			_file.setReadable(true);
			_file.setWritable(true);
		
			IOUtils.copyLarge(is, fos);
			fos.flush();
			fos.close();

			CloudResultInfo result = cloudFileManager.uploadAndShareFile(folderName, name, _file, mimeType);
			_file.delete();
			return result;
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
				
				File _file = new File(File.separator+"tmp"+File.separator+UUID.randomUUID().toString()+".tmp");
				
				FileOutputStream fos = new FileOutputStream(_file);				
				_file.setReadable(true);
				_file.setWritable(true);
			
				IOUtils.copyLarge(file.getInputStream(), fos);
				fos.flush();
				fos.close();

				result.add(cloudFileManager.uploadAndShareFile(folder, names[index], _file, mimeType));
				_file.delete();					
				
				index++;
			}
			return result;
		} catch (Exception e) {
			throw new AnterosCloudIntegrationServerException(e);
		}
	}
	
	

}