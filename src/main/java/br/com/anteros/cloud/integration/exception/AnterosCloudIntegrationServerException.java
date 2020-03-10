package br.com.anteros.cloud.integration.exception;

public class AnterosCloudIntegrationServerException extends RuntimeException {

	public AnterosCloudIntegrationServerException() {
		super();
	}

	public AnterosCloudIntegrationServerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AnterosCloudIntegrationServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public AnterosCloudIntegrationServerException(String message) {
		super(message);
	}

	public AnterosCloudIntegrationServerException(Throwable cause) {
		super(cause);
	}

}
