package agent.communication.request;

import repast.simphony.space.grid.GridPoint;
import agent.communication.info.Information;

/**
 * Requests other agents to send information of a certain type at a certain
 * position if existing in their belief.
 */
public class InformationRequest extends Request
{
	private Class<? extends Information> informationClass;

	public InformationRequest(String senderID, int importance,
			GridPoint position, Class<? extends Information> informationClass)
	{
		super(senderID, importance, position);
		this.informationClass = informationClass;
	}

	public Class<? extends Information> getInformationClass()
	{
		return informationClass;
	}

}
