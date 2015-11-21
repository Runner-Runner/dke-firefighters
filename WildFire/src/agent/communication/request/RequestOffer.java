package agent.communication.request;

public class RequestOffer{
	private double distance;
	private String senderId;
	private int requestID;
	
	public RequestOffer(String senderId, int requestID, double distance) {
		super();
		this.requestID = requestID;
		this.distance = distance;
		this.senderId = senderId;
	}

	public double getDistance() {
		return distance;
	}

	public String getSenderId() {
		return senderId;
	}

	public int getRequestID() {
		return requestID;
	}
	
	
}
