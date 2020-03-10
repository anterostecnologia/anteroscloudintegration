package br.com.anteros.cloud.integration.filesharing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CloudShareFolder {

	private String folderName;

	private CloudShareFolder(String folderName) {
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public static Collection<CloudShareFolder> list(List<String> listFolderContent) {
		Collection<CloudShareFolder> result = new ArrayList<CloudShareFolder>();
		for (String s : listFolderContent) {
			result.add(CloudShareFolder.of(s));
		}
		return result;
	}

	private static CloudShareFolder of(String folderName) {
		return new CloudShareFolder(folderName);
	}	

}
