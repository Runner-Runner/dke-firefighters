package agent.communication.request;

import agent.bdi.Action;

public class ActionRequest extends Request{
	private Action action;
	
	public ActionRequest(int importance, Integer positionX, Integer positionY, Action action, String senderID) {
		super(senderID, importance, positionX, positionY);
		this.action = action;
	}

	public Action getAction() {
		return action;
	}
	
	
	
}
