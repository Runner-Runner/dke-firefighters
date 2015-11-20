package agent;

public class Intention {
	private Action action;
	private Integer xPosition;
	private Integer yPosition;

	public Intention(Action action, Integer xPosition, Integer yPosition) {
		super();
		this.action = action;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
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
