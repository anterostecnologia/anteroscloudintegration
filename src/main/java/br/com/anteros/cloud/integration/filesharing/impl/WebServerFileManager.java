package br.com.anteros.cloud.integration.filesharing.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import br.com.anteros.cloud.integration.exception.AnterosCloudIntegrationServerException;
import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.cloud.integration.filesharing.CloudResultInfo;
import br.com.anteros.cloud.integration.filesharing.CloudFile;
import br.com.anteros.cloud.integration.filesharing.CloudShareFolder;
import br.com.anteros.core.utils.IOUtils;
import br.com.anteros.core.utils.StringUtils;

public class WebServerFileManager implements CloudFileManager {
	private String uploadFolder = (System.getProperty("java.io.tmpdir"));
	private String urlSite = "http://localhost";

	public WebServerFileManager() {

	}

	public WebServerFileManager(String uploadFolder, String urlSite) {

	}

	@Override
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, byte[] fileContent, String mimeType)
			throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = "";
		}

		try {
			File fld = new File(uploadFolder + File.separator + folderName);

			if (!fld.exists()) {
				fld.mkdir();
			}

			File arquivo = new File(fld, fileName);
			FileOutputStream fos = new FileOutputStream(arquivo);
			fos.write(fileContent);
			fos.flush();
			fos.close();

			arquivo.setReadable(true, false);
			return CloudResultInfo.of(urlSite + "/" + fileName, 0L, fileName);
		} catch (Exception exception) {
			throw new AnterosCloudIntegrationServerException(
					"O upload do arquivo falhou " + fileName + " => " + exception.getMessage());
		}

	}

	@Override
	public void removeFile(String folderName, String fileName) throws Exception {
		File file = new File(fileName);
		if (file.exists()) {
			try {
				file.delete();
			} catch (Exception exception) {
				throw new AnterosCloudIntegrationServerException(
						"Não foi possível remover o arquivo " + fileName + " => " + exception.getMessage());
			}
		} else {
			throw new AnterosCloudIntegrationServerException("O arquivo " + fileName + " não foi encontrado.");
		}

	}

	@Override
	public void createFolder(String folderName) throws Exception {
		String[] folderNameSplit = folderName.split("\\/");
		String fldTemp = "";
		boolean appendDelimiter = false;
		for (String fld : folderNameSplit) {
			if (appendDelimiter) {
				fldTemp += File.separator;
			}
			fldTemp += fld;
			File _folder = new File(fldTemp);
			if (!_folder.exists()) {
				_folder.mkdir();
			}
			appendDelimiter = true;
		}
	}

	@Override
	public void removeFolder(String folderName) throws Exception {
		File _folder = new File(folderName);
		if (_folder.exists()) {
			_folder.delete();
		}

	}

	@Override
	public Collection<CloudFile> listFiles(String folderName) throws Exception {
		File fld = new File(uploadFolder + File.separator + folderName);
		if (!fld.exists()) {
			throw new AnterosCloudIntegrationServerException(
					"Pasta não encontrada " + folderName );
		}
		Collection<CloudFile> result = new ArrayList<CloudFile>();
		try {
			for (File f : fld.listFiles()) {
				CloudFile share = new CloudFile();
				share.setFileTarget(f.getName());
				share.setPath(f.getAbsolutePath());
				share.setUrl(urlSite+"/uploads/"+folderName+"/"+f.getName());
				result.add(share);
			}
			return result;
		} catch (Exception exception) {
			throw new AnterosCloudIntegrationServerException(
					"Ocorreu um erro ao obter listar os arquivos da pasta " + folderName + " => " + exception.getMessage());
		}
	}

	@Override
	public Collection<CloudFile> listImages(String folderName) throws Exception {
		File fld = new File(uploadFolder + File.separator + folderName);
		if (!fld.exists()) {
			throw new AnterosCloudIntegrationServerException(
					"Pasta não encontrada " + folderName );
		}
		Collection<CloudFile> result = new ArrayList<CloudFile>();
		try {
			FilenameFilter filter = new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					if (((name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif") || 
							name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")))){
					return true;
					}
					else return false;
				}
			};
			for (File f : fld.listFiles(filter)) {
				CloudFile share = new CloudFile();
				share.setFileTarget(f.getName());
				share.setPath(f.getAbsolutePath());
				share.setUrl(urlSite+"/uploads/"+folderName+"/"+f.getName());
				result.add(share);
			}
			return result;
		} catch (Exception exception) {
			throw new AnterosCloudIntegrationServerException(
					"Ocorreu um erro ao obter listar os arquivos da pasta " + folderName + " => " + exception.getMessage());
		}
	}

	@Override
	public Collection<CloudShareFolder> listFolders(String folderName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUploadFolder() {
		return uploadFolder;
	}

	public void setUploadFolder(String uploadFolder) {
		this.uploadFolder = uploadFolder;
	}

	public String getUrlSite() {
		return urlSite;
	}

	public void setUrlSite(String urlSite) {
		this.urlSite = urlSite;
	}

	@Override
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, File file, String mimeType)
			throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = "";
		}

		try {
			File fld = new File(uploadFolder + File.separator + folderName);

			if (!fld.exists()) {
				fld.mkdir();
			}
			
			String fs = IOUtils.readFileToString(file, Charset.defaultCharset());

			File arquivo = new File(fld, fileName);
			FileOutputStream fos = new FileOutputStream(arquivo);
			fos.write(fs.getBytes());
			fos.flush();
			fos.close();

			arquivo.setReadable(true, false);
			return CloudResultInfo.of(urlSite + "/" + fileName, 0L, fileName);
		} catch (Exception exception) {
			throw new AnterosCloudIntegrationServerException(
					"O upload do arquivo falhou " + fileName + " => " + exception.getMessage());
		}
	}

}
