package net.hunnor.dict.util;

public class Request {

	private RequestStatus status;
	private Object response;

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public RequestStatus status() {
		return status;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public Object response() {
		return response;
	}

}
