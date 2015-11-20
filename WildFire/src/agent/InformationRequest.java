package agent;

public class InformationRequest extends Request{
	private Class<? extends Information> information;
	public InformationRequest(int importance, Integer positionX, Integer positionY, Class<? extends Information> information) {
		super(importance, positionX, positionY);
		this.information = information;
	}
	public Class<? extends Information> getInformation() {
		return information;
	}

	
}
