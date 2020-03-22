package br.com.anteros.cloud.integration.filesharing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import br.com.anteros.nextcloud.api.filesharing.Share;
import br.com.anteros.nextcloud.api.webdav.FolderItemDetail;

public class CloudFile {
	private int id;
	private ShareType shareType;
	private String ownerId;
	private String ownerDisplayName;
	private SharePermissions sharePermissions;
	private String fileOwnerId;
	private String fileOwnerDisplayName;
	private String path;
	private ItemType itemType;
	private String fileTarget;
	private String shareWithId;
	private String shareWithDisplayName;
	private String token;
	private Instant shareTime;
	private LocalDate expiration;
	private String url;
	private String mimetype;
	private Long contentLength;
	private Date creation;
	private Date modified;
	private String displayName;
	private boolean isDirectory;
	private String name;

	private CloudFile(int id, ShareType shareType, String ownerId, String ownerDisplayName,
			SharePermissions sharePermissions, String fileOwnerId, String fileOwnerDisplayName, String path,
			ItemType itemType, String fileTarget, String shareWithId, String shareWithDisplayName, String token,
			Instant shareTime, LocalDate expiration, String url, String mimetype) {
		super();
		this.id = id;
		this.shareType = shareType;
		this.ownerId = ownerId;
		this.ownerDisplayName = ownerDisplayName;
		this.sharePermissions = sharePermissions;
		this.fileOwnerId = fileOwnerId;
		this.fileOwnerDisplayName = fileOwnerDisplayName;
		this.path = path;
		this.itemType = itemType;
		this.fileTarget = fileTarget;
		this.shareWithId = shareWithId;
		this.shareWithDisplayName = shareWithDisplayName;
		this.token = token;
		this.shareTime = shareTime;
		this.expiration = expiration;
		this.url = url;
		this.mimetype = mimetype;
	}

	private CloudFile(String name, String displayName, String contentType, Date creation, Date modified,
			boolean directory, Long contentLength) {
		this.name = name;
		this.displayName = displayName;
		this.mimetype = contentType;
		this.creation = creation;
		this.modified = modified;
		this.isDirectory = directory;
		this.contentLength = contentLength;
	}

	public CloudFile() {
	}

	public int getId() {
		return id;
	}

	public ShareType getShareType() {
		return shareType;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public SharePermissions getSharePermissions() {
		return sharePermissions;
	}

	public String getFileOwnerId() {
		return fileOwnerId;
	}

	public String getFileOwnerDisplayName() {
		return fileOwnerDisplayName;
	}

	public String getPath() {
		return path;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public String getFileTarget() {
		return fileTarget;
	}

	public String getShareWithId() {
		return shareWithId;
	}

	public String getShareWithDisplayName() {
		return shareWithDisplayName;
	}

	public String getToken() {
		return token;
	}

	public Instant getShareTime() {
		return shareTime;
	}

	public LocalDate getExpiration() {
		return expiration;
	}

	public String getUrl() {
		return url;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setFileTarget(String fileTarget) {
		this.fileTarget = fileTarget;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setShareType(ShareType shareType) {
		this.shareType = shareType;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

	public void setSharePermissions(SharePermissions sharePermissions) {
		this.sharePermissions = sharePermissions;
	}

	public void setFileOwnerId(String fileOwnerId) {
		this.fileOwnerId = fileOwnerId;
	}

	public void setFileOwnerDisplayName(String fileOwnerDisplayName) {
		this.fileOwnerDisplayName = fileOwnerDisplayName;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	public void setShareWithId(String shareWithId) {
		this.shareWithId = shareWithId;
	}

	public void setShareWithDisplayName(String shareWithDisplayName) {
		this.shareWithDisplayName = shareWithDisplayName;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setShareTime(Instant shareTime) {
		this.shareTime = shareTime;
	}

	public void setExpiration(LocalDate expiration) {
		this.expiration = expiration;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public static CloudFile of(int id, ShareType shareType, String ownerId, String ownerDisplayName,
			SharePermissions sharePermissions, String fileOwnerId, String fileOwnerDisplayName, String path,
			ItemType itemType, String fileTarget, String shareWithId, String shareWithDisplayName, String token,
			Instant shareTime, LocalDate expiration, String url, String mimetype) {
		return new CloudFile(id, shareType, ownerId, ownerDisplayName, sharePermissions, fileOwnerId,
				fileOwnerDisplayName, path, itemType, fileTarget, shareWithId, shareWithDisplayName, token, shareTime,
				expiration, url, mimetype);
	}

	public static Collection<CloudFile> list(Collection<Share> shares, List<FolderItemDetail> details) {
		Collection<CloudFile> result = new ArrayList<>();

		for (FolderItemDetail detail : details) {

			CloudFile cloudFile = CloudFile.of(detail.getName(), detail.getDisplayName(), detail.getContentType(),
					detail.getCreation(), detail.getModified(), detail.isDirectory(), detail.getContentLength());

			for (Share share : shares) {
				String target = share.getFileTarget();
				String name = cloudFile.getName();
				if (target.contains(name)) {
					cloudFile.setId(share.getId());
					cloudFile.setShareType(ShareType.getShareTypeForIntValue(share.getShareType().getIntValue()));
					cloudFile.setOwnerDisplayName(share.getOwnerDisplayName());
					cloudFile.setSharePermissions(
							new SharePermissions(share.getSharePermissions().getCurrentPermission()));
					cloudFile.setOwnerId(share.getFileOwnerId());
					cloudFile.setFileOwnerDisplayName(share.getFileOwnerDisplayName());
					cloudFile.setPath(share.getPath());
					cloudFile.setItemType(ItemType.getItemByName(share.getItemType().getItemTypeStr()));
					cloudFile.setFileTarget(share.getFileTarget());
					cloudFile.setShareWithId(share.getShareWithId());
					cloudFile.setShareWithDisplayName(share.getShareWithDisplayName());
					cloudFile.setToken(share.getToken());
					cloudFile.setShareTime(share.getShareTime());
					cloudFile.setExpiration(share.getExpiration());
					cloudFile.setMimetype(share.getMimetype());
					cloudFile.setUrl(share.getUrl());
				}
			}
			result.add(cloudFile);
		}
		return result;
	}

	private static CloudFile of(String name, String displayName, String contentType, Date creation, Date modified,
			boolean directory, Long contentLength) {
		return new CloudFile(name, displayName, contentType, creation, modified, directory, contentLength);
	}

	public Long getContentLength() {
		return contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	public Date getCreation() {
		return creation;
	}

	public void setCreation(Date creation) {
		this.creation = creation;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Collection<CloudFile> list(Collection<Share> shares) {
		Collection<CloudFile> result = new ArrayList<>();
		for (Share share : shares) {
			CloudFile cloudFile = new CloudFile();
			cloudFile.setId(share.getId());
			cloudFile.setShareType(ShareType.getShareTypeForIntValue(share.getShareType().getIntValue()));
			cloudFile.setOwnerDisplayName(share.getOwnerDisplayName());
			cloudFile.setSharePermissions(new SharePermissions(share.getSharePermissions().getCurrentPermission()));
			cloudFile.setOwnerId(share.getFileOwnerId());
			cloudFile.setFileOwnerDisplayName(share.getFileOwnerDisplayName());
			cloudFile.setPath(share.getPath());
			cloudFile.setItemType(ItemType.getItemByName(share.getItemType().getItemTypeStr()));
			cloudFile.setFileTarget(share.getFileTarget());
			cloudFile.setShareWithId(share.getShareWithId());
			cloudFile.setShareWithDisplayName(share.getShareWithDisplayName());
			cloudFile.setToken(share.getToken());
			cloudFile.setShareTime(share.getShareTime());
			cloudFile.setExpiration(share.getExpiration());
			cloudFile.setUrl(share.getUrl());
			cloudFile.setMimetype(share.getMimetype());
			result.add(cloudFile);
		}

		return result;
	}
}