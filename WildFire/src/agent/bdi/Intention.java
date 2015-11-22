package agent.bdi;


public class Intention {
	private Action action;
	private Integer xPosition;
	private Integer yPosition;
	private String requester;

	public Intention(Action action, Integer xPosition, Integer yPosition, String requester) {
		super();
		this.action = action;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.requester = requester;
	}

	public String getRequester() {
		return requester;
	}

	public Action getAction() {
		return action;
	}

	public Integer getxPosition() {
		return xPosition;
	}

	public Integer getyPosition() {
		return yPosition;
	}
	
	
}
