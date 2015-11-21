package agent.communication.request;

import agent.communication.info.Information;


public class InformationRequest extends Request{
	private Class<? extends Information> information;
	public InformationRequest(String senderID, int importance, Integer positionX, Integer positionY, Class<? extends Information> information) {
		super(senderID, importance, positionX, positionY);
		this.information = information;
	}
	public Class<? extends Information> getInformation() {
		return information;
	}

	
}
