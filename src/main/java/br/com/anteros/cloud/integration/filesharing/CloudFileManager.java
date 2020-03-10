package br.com.anteros.cloud.integration.filesharing;

import java.util.Collection;

public interface CloudFileManager {	
	
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, byte[] fileContent, String mimeType) throws Exception;	

	public void removeFile(String folderName, String fileName) throws Exception;
	
	public void createFolder(String folderName) throws Exception;
	
	public void removeFolder(String folderName) throws Exception;  
	
	public Collection<CloudFile> listFiles(String folderName) throws Exception;  
	
	public Collection<CloudFile> listImages(String folderName) throws Exception;
	
	public Collection<CloudShareFolder> listFolders(String folderName) throws Exception;  
	

}
