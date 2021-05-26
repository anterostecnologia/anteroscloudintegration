package br.com.anteros.cloud.integration.filesharing;

public class CloudResultInfo {

	String sharedLink;
	Long fileSize;
	String fileName;
	
	private CloudResultInfo(String url, Long fileSize, String fileName) {
		this.sharedLink = url;
		this.fileSize = fileSize;
		this.fileName = fileName;
	}
	
	public CloudResultInfo() {
	}

	public String getSharedLink() {
		return sharedLink;
	}
	public void setSharedLink(String sharedLink) {
		this.sharedLink = sharedLink;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public static CloudResultInfo of(String url, Long fileSize, String fileName) {
		return new CloudResultInfo(url, fileSize, fileName);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	@Override
	public String toString() {
		return "CloudResultInfo{" +
				"sharedLink='" + sharedLink + '\'' +
				", fileSize=" + fileSize +
				", fileName='" + fileName + '\'' +
				'}';
	}
}
