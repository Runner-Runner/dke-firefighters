package agent.communication.request;

import agent.communication.info.Information;


public class InformationRequest extends Request{
	private Class<? extends Information> informationClass;
	public InformationRequest(String senderID, int importance, Integer positionX, Integer positionY, Class<? extends Information> informationClass) {
		super(senderID, importance, positionX, positionY);
		this.informationClass = informationClass;
	}
	public Class<? extends Information> getInformationClass() {
		return informationClass;
	}

	
}
