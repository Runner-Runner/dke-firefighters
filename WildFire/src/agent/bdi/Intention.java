package agent.bdi;

import repast.simphony.space.grid.GridPoint;


public class Intention {
	private Action action;
	private GridPoint position;
	private String requesterId;
	private Integer requestId;

	public Intention(Action action, GridPoint position, String requesterId, Integer requestId) {
		super();
		this.action = action;
		this.position = position;
		this.requesterId = requesterId;
		this.requestId = requestId;
	}

	public String getRequesterId() {
		return requesterId;
	}
	public Integer getRequestId(){
		return requestId;
	}

	public Action getAction() {
		return action;
	}

	public GridPoint getPosition(){
		return this.position;
	}
	
	
}
