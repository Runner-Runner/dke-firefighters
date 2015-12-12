package agent.communication.request;

/**
 * Provides a dismiss towards agents that were already confirmed as executor of
 * the given request. This can happen when for example the requested action
 * suddenly becomes not executable anymore.
 * 
 * @author Daniel
 *
 */
public class RequestDismiss
{
	private int requestID;
	private String senderID;

	public RequestDismiss(int requestID, String senderID)
	{
		super();
		this.requestID = requestID;
		this.senderID = senderID;
	}

	public int getRequestID()
	{
		return requestID;
	}

	public String getSenderID()
	{
		return senderID;
	}

}
