package br.com.anteros.cloud.integration.filesharing.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import br.com.anteros.cloud.integration.filesharing.CloudFile;
import br.com.anteros.cloud.integration.filesharing.CloudFileManager;
import br.com.anteros.cloud.integration.filesharing.CloudResultInfo;
import br.com.anteros.cloud.integration.filesharing.CloudShareFolder;

public class AmazonS3FileManager implements CloudFileManager {

	private AmazonS3Client s3Client;
	private Bucket b;
	private static final String SUFFIX = "/";

	public AmazonS3FileManager(String accessKeyId, String secretKeyId, String region, String bucketName) {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretKeyId);
		
		s3Client = new AmazonS3Client(awsCreds,Region.getRegion(region));
		
		if (s3Client.doesBucketExist(bucketName)) {
			b = getBucket(bucketName);
		} else {
			try {
				b = s3Client.createBucket(bucketName);
			} catch (AmazonS3Exception e) {
				System.err.println(e.getErrorMessage());
			}
		}
	}

	public Bucket getBucket(String bucket_name) {
		Bucket named_bucket = null;
		List<Bucket> buckets = s3Client.listBuckets();
		for (Bucket b : buckets) {
			if (b.getName().equals(bucket_name)) {
				named_bucket = b;
			}
		}
		return named_bucket;
	}

	@Override
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, byte[] fileContent, String mimeType)
			throws Exception {

		try {
			this.createFolder(folderName);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(mimeType);
			metadata.setContentLength(fileContent.length);

			PutObjectRequest putObjectRequest = new PutObjectRequest(b.getName(), folderName + SUFFIX + fileName,
					new ByteArrayInputStream(fileContent), metadata);
			AccessControlList acl = new AccessControlList();
			PutObjectResult objectResult = s3Client.putObject(putObjectRequest);

			s3Client.setObjectAcl(b.getName(), folderName + SUFFIX + fileName, CannedAccessControlList.PublicRead);
			URL url = s3Client.getUrl(b.getName(), fileName);
			String sharableLink = url.toExternalForm();

			CloudResultInfo result = new CloudResultInfo();
			result.setSharedLink(sharableLink);
			result.setFileSize(Long.valueOf(fileContent.length));
			result.setFileName(fileName);

		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}

		return null;
	}

	@Override
	public void removeFile(String folderName, String fileName) throws Exception {
		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(b.getName(), folderName + SUFFIX + fileName);
		s3Client.deleteObject(deleteObjectRequest);
	}

	@Override
	public void createFolder(String folderName) throws Exception {
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
		try {
			boolean exists = s3Client.doesObjectExist(b.getName(), folderName + SUFFIX);
			if (!exists) {
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(0);
				PutObjectRequest putObjectRequest = new PutObjectRequest(b.getName(), folderName + SUFFIX, emptyContent,
						metadata);

				s3Client.putObject(putObjectRequest);
			}
		} catch (Exception e) {
		} finally {
			emptyContent.close();
		}
	}

	@Override
	public void removeFolder(String folderName) throws Exception {
		List<S3ObjectSummary> list = s3Client.listObjects(b.getName(), folderName).getObjectSummaries();
		for (S3ObjectSummary file : list) {
			s3Client.deleteObject(b.getName(), file.getKey());
		}
		s3Client.deleteObject(b.getName(), folderName);
	}

	@Override
	public Collection<CloudFile> listFiles(String folderName) throws Exception {

		List<S3ObjectSummary> list = s3Client.listObjects(b.getName(), folderName).getObjectSummaries();

		Collection<CloudFile> result = new ArrayList<CloudFile>();
		for (S3ObjectSummary file : list) {
			URL url = s3Client.getUrl(b.getName(), file.getKey());
			S3Object s3Object = s3Client.getObject(new GetObjectRequest(b.getName(), file.getKey()));
			String sharableLink = url.toExternalForm();
			CloudFile cf = new CloudFile();
			cf.setContentLength(s3Object.getObjectMetadata().getContentLength());
			cf.setDirectory(false);
			cf.setModified(file.getLastModified());
			cf.setName(file.getKey());
			cf.setMimetype(s3Object.getObjectMetadata().getContentType());
			cf.setUrl(sharableLink);
			result.add(cf);
		}

		return result;
	}

	@Override
	public Collection<CloudFile> listImages(String folderName) throws Exception {
		List<S3ObjectSummary> list = s3Client.listObjects(b.getName(), folderName).getObjectSummaries();

		Collection<CloudFile> result = new ArrayList<CloudFile>();
		for (S3ObjectSummary file : list) {

			S3Object s3Object = s3Client.getObject(new GetObjectRequest(b.getName(), file.getKey()));
			if (s3Object.getObjectMetadata().getContentType().contains("image")) {
				URL url = s3Client.getUrl(b.getName(), file.getKey());
				String sharableLink = url.toExternalForm();

				CloudFile cf = new CloudFile();
				cf.setContentLength(s3Object.getObjectMetadata().getContentLength());
				cf.setDirectory(false);
				cf.setModified(file.getLastModified());
				cf.setName(file.getKey());
				cf.setMimetype(s3Object.getObjectMetadata().getContentType());
				cf.setUrl(sharableLink);
				result.add(cf);
			}
		}

		return result;
	}

	@Override
	public Collection<CloudShareFolder> listFolders(String folderName) throws Exception {
		return CloudShareFolder.list(s3Client.listObjects(b.getName(), folderName).getCommonPrefixes());
	}

	@Override
	public CloudResultInfo uploadAndShareFile(String folderName, String fileName, File file, String mimeType)
			throws Exception {
		try {
			this.createFolder(folderName);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(mimeType);
			metadata.setContentLength(file.length());
			
			FileInputStream fin = new FileInputStream(file);

			PutObjectRequest putObjectRequest = new PutObjectRequest(b.getName(), folderName + SUFFIX + fileName,
					fin, metadata);
			AccessControlList acl = new AccessControlList();
			PutObjectResult objectResult = s3Client.putObject(putObjectRequest);

			s3Client.setObjectAcl(b.getName(), folderName + SUFFIX + fileName, CannedAccessControlList.PublicRead);
			URL url = s3Client.getUrl(b.getName(), fileName);
			String sharableLink = url.toExternalForm();

			CloudResultInfo result = new CloudResultInfo();
			result.setSharedLink(sharableLink);
			result.setFileSize(Long.valueOf(file.length()));
			result.setFileName(fileName);

		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}

		return null;
	}

}
