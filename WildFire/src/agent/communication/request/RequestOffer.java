package agent.communication.request;

/**
 * Provides the offer of an agent to except and carry out an action request from
 * another agent. Also stores different decision criteria for the request sender to evaluate.
 */
public class RequestOffer
{
	/**
	 * The distance to the action's execution position.
	 */
	private double distance;
	/**
	 * Whether this agent already has another intention he/she wants to fulfill.
	 */
	private boolean otherIntention;
	private String senderId;
	private int requestID;

	public RequestOffer(String senderId, int requestID, double distance,
			boolean otherIntention)
	{
		super();
		this.requestID = requestID;
		this.distance = distance;
		this.senderId = senderId;
		this.otherIntention = otherIntention;
	}

	public double getDistance()
	{
		return distance;
	}

	public String getSenderId()
	{
		return senderId;
	}

	public int getRequestID()
	{
		return requestID;
	}

	public boolean hasOtherIntention()
	{
		return otherIntention;
	}

}
