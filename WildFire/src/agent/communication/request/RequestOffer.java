package agent.communication.request;

public class RequestOffer{
	private double distance;
	private String senderId;
	private boolean otherIntention;
	private int requestID;
	
	public RequestOffer(String senderId, int requestID, double distance, boolean otherIntention) {
		super();
		this.requestID = requestID;
		this.distance = distance;
		this.senderId = senderId;
		this.otherIntention = otherIntention;
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
	
	public boolean hasOtherIntention(){
		return otherIntention;
	}
	
}
