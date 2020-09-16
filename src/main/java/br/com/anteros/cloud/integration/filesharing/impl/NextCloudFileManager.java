package br.com.anteros.cloud.integration.filesharing.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import br.com.anteros.cloud.integration.exception.AnterosCloudIntegrationServerException;
import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.cloud.integration.filesharing.CloudResultInfo;
import br.com.anteros.cloud.integration.filesharing.CloudFile;
import br.com.anteros.cloud.integration.filesharing.CloudShareFolder;
import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.nextcloud.api.AnterosNextCloudConnector;
import br.com.anteros.nextcloud.api.filesharing.Share;
import br.com.anteros.nextcloud.api.webdav.FolderItemDetail;

public class NextCloudFileManager implements CloudFileManager {

	AnterosNextCloudConnector connector;
	private String defaultFolder;

	public NextCloudFileManager(String serverName, boolean useHttps, int port, String username, String password,
			String defaultFolder) {
		connector = new AnterosNextCloudConnector(serverName, useHttps, port, username, password);
		this.defaultFolder = defaultFolder;
	}

	@Override
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, byte[] fileContent, String mimeType) throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			fileName = UUID.randomUUID().toString();
		}
		if (StringUtils.isEmpty(folderName)) {
			folderName = defaultFolder;
		}

		String[] folderNameSplit = folderName.split("\\/");
		String fldTemp = "";
		boolean appendDelimiter = false;
		for (String fld : folderNameSplit) {
			if (appendDelimiter) {
				fldTemp += "/";
			}
			fldTemp += fld;
			if (!connector.folderExists(fldTemp)) {
				connector.createFolder(fldTemp);
			}
			appendDelimiter = true;
		}

		if (connector.fileExists(folderName + File.separator + fileName)) {
			throw new ExternalFileManagerException("Arquivo já existe " + folderName + File.separator + fileName);
		}
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(fileContent);
			connector.uploadFile(bais, folderName + File.separator + fileName);
			Share doShare = connector.doShare(folderName + File.separator + fileName, br.com.anteros.nextcloud.api.filesharing.ShareType.PUBLIC_LINK, "", false,
					"", new br.com.anteros.nextcloud.api.filesharing.SharePermissions(1));
			
			String sharedLink = null;
			if (mimeType.contains("image")) {
				sharedLink = doShare.getUrl() + "/preview#" + fileName;
			} else {
				sharedLink = doShare.getUrl() + "/download#" + fileName;
			}			
			return CloudResultInfo.of(sharedLink, new Long(fileContent.length), fileName);
		} catch (Exception exception) {
			throw new ExternalFileManagerException(
					"O upload do arquivo falhou " + fileName + " => " + exception.getMessage());
		}
	}

	@Override
	public void removeFile(String folderName, String fileName) throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = defaultFolder;
		}

		if (!connector.fileExists(folderName + File.separator + fileName)) {
			throw new ExternalFileManagerException("Arquivo não encontrado " + folderName + File.separator + fileName);
		}
		try {
			connector.removeFile(folderName + File.separator + fileName);
		} catch (Exception exception) {
			throw new ExternalFileManagerException("Não foi possível remover o arquivo " + folderName + File.separator
					+ fileName + " => " + exception.getMessage());
		}

	}

	@Override
	public Collection<CloudFile> listFiles(String folderName) throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = "";
		}
		if (!connector.folderExists(folderName)) {
			throw new AnterosCloudIntegrationServerException(
					"A pasta "+folderName+" não existe");
		}
		try {
			Collection<Share> shares = connector.getShares(folderName, true, true);
			
			List<FolderItemDetail> listDetailsFolderContent = connector.listDetailsFolderContent(folderName, 1,true);
			
			
			return CloudFile.list(shares, listDetailsFolderContent);
		} catch (Exception exception) {
			throw new AnterosCloudIntegrationServerException(
					"Ocorreu um erro ao obter listar os arquivos da pasta " + folderName + " => " + exception.getMessage());
		}
		
	}

	@Override
	public Collection<CloudFile> listImages(String folderName) throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = "";
		}

		if (!connector.folderExists(folderName)) {
			throw new AnterosCloudIntegrationServerException(
					"A pasta "+folderName+" não existe");
		}
		
		try {
			Collection<Share> shares = connector.getShares(folderName, true, true);
			Collection<Share> result = new ArrayList<Share>();
			for (Share share : shares) {
				if ("image/png".equals(share.getMimetype()) || "image/jpeg".equals(share.getMimetype())){
					result.add(share);
				}
			}
			return CloudFile.list(shares);
		} catch (Exception exception) {
			throw new AnterosCloudIntegrationServerException(
					"Ocorreu um erro ao obter listar as imagens da pasta " + folderName + " => " + exception.getMessage());
		}		
	}

	@Override
	public void createFolder(String folderName) throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = defaultFolder;
		}

		String[] folderNameSplit = folderName.split("\\/");
		String fldTemp = "";
		boolean appendDelimiter = false;
		for (String fld : folderNameSplit) {
			if (appendDelimiter) {
				fldTemp += "/";
			}
			fldTemp += fld;
			if (!connector.folderExists(fldTemp)) {
				connector.createFolder(fldTemp);
			}
			appendDelimiter = true;
		}		
	}

	@Override
	public void removeFolder(String folderName) throws Exception {
		if (connector.folderExists(folderName)) {
			connector.deleteFolder(folderName);		
		} else {
			throw new AnterosCloudIntegrationServerException(
					"A pasta "+folderName+" não existe");
		}
	}

	@Override
	public Collection<CloudShareFolder> listFolders(String folderName) throws Exception {
		String[] folderNameSplit = folderName.split("\\/");
		String fldTemp = "";
		boolean appendDelimiter = false;
		for (String fld : folderNameSplit) {
			if (appendDelimiter) {
				fldTemp += "/";
			}
			fldTemp += fld;
			if (!connector.folderExists(fldTemp)) {
				connector.createFolder(fldTemp);
			}
			appendDelimiter = true;
		}		
		return CloudShareFolder.list(connector.listFolderContent(folderName));
	}

	@Override
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, File file, String mimeType)
			throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			fileName = UUID.randomUUID().toString();
		}
		if (StringUtils.isEmpty(folderName)) {
			folderName = defaultFolder;
		}

		String[] folderNameSplit = folderName.split("\\/");
		String fldTemp = "";
		boolean appendDelimiter = false;
		for (String fld : folderNameSplit) {
			if (appendDelimiter) {
				fldTemp += "/";
			}
			fldTemp += fld;
			if (!connector.folderExists(fldTemp)) {
				connector.createFolder(fldTemp);
			}
			appendDelimiter = true;
		}

		if (connector.fileExists(folderName + File.separator + fileName)) {
			throw new ExternalFileManagerException("Arquivo já existe " + folderName + File.separator + fileName);
		}
		try {
			long fileSize = file.length();
			connector.uploadFile(file, folderName + File.separator + fileName);
			Share doShare = connector.doShare(folderName + File.separator + fileName, br.com.anteros.nextcloud.api.filesharing.ShareType.PUBLIC_LINK, "", false,
					"", new br.com.anteros.nextcloud.api.filesharing.SharePermissions(1));
			
			String sharedLink = null;
			if (mimeType.contains("image")) {
				sharedLink = doShare.getUrl() + "/preview#" + fileName;
			} else {
				sharedLink = doShare.getUrl() + "/download#" + fileName;
			}			
			return CloudResultInfo.of(sharedLink, fileSize, fileName);
		} catch (Exception exception) {
			throw new ExternalFileManagerException(
					"O upload do arquivo falhou " + fileName + " => " + exception.getMessage());
		}
	}

}
