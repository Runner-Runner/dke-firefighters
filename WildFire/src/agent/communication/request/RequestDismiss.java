package agent.communication.request;

public class RequestDismiss {
	private int requestID;
	private String senderID;
	public RequestDismiss(int requestID, String senderID) {
		super();
		this.requestID = requestID;
		this.senderID = senderID;
	}
	public int getRequestID() {
		return requestID;
	}
	public String getSenderID() {
		return senderID;
	}
	
	
}
