package agent.bdi;


public class Intention {
	private Action action;
	private Integer xPosition;
	private Integer yPosition;
	private String requesterId;

	public Intention(Action action, Integer xPosition, Integer yPosition, String requesterId) {
		super();
		this.action = action;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.requesterId = requesterId;
	}

	public String getRequesterId() {
		return requesterId;
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
