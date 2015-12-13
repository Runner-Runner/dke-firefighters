package agent.communication.request;

/**
 * Provides confirmation to an offering agent. An agent only is accepted to
 * execute a request if he/she receives a confirmation.
 */
public class RequestConfirm
{
	private String senderID;
	private int requestID;

	public RequestConfirm(String senderID, int requestID)
	{
		super();
		this.senderID = senderID;
		this.requestID = requestID;
	}

	public String getSenderID()
	{
		return senderID;
	}

	public int getRequestID()
	{
		return requestID;
	}

}
