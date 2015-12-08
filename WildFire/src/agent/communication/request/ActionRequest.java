package agent.communication.request;

import repast.simphony.space.grid.GridPoint;
import agent.bdi.Action;

public class ActionRequest extends Request
{
	private Action action;

	public ActionRequest(int importance, GridPoint position, Action action,
			String senderID)
	{
		super(senderID, importance, position);
		this.action = action;
	}

	public Action getAction()
	{
		return action;
	}

}
