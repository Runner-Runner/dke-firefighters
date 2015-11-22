package agent.communication.request;

public class RequestDismiss {
	private String requestID;
	private String senderID;
	public RequestDismiss(String requestID, String senderID) {
		super();
		this.requestID = requestID;
		this.senderID = senderID;
	}
	public String getRequestID() {
		return requestID;
	}
	public String getSenderID() {
		return senderID;
	}
	
	
}
