package net.hunnor.dict.util;

public enum RequestStatus {

	/**
	 * <p>The request is processed successfully
	 */
	OK,

	/**
	 * <p>I/O error during a network operation 
	 */
	IO_EXCEPTION_NETWORK,

	/**
	 * The supplied string is not a valid URL
	 */
	MALFORMED_URL_EXCEPTION,

	/**
	 * <p>A network operation is requested while the device is offline
	 */
	NET_NOT_READY,

	/**
	 * <p>Error during an HTTP request
	 */
	PROTOCOL_EXCEPTION;

}
