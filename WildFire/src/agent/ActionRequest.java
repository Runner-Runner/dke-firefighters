package agent;

public class ActionRequest extends Request{
	private Class<? extends Action> action;
	
	public ActionRequest(int importance, Integer positionX, Integer positionY, Class<? extends Action> action) {
		super(importance, positionX, positionY);
		this.action = action;
	}

	public Class<? extends Action> getAction() {
		return action;
	}
	
	
	
}
