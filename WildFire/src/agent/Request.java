package agent;

public abstract class Request {
	//TODO maybe use enum for importance
	protected int importance;
	protected Integer positionX;
	protected Integer positionY;
	
	public Request(int importance, Integer positionX, Integer positionY) {
		super();
		this.importance = importance;
		this.positionX = positionX;
		this.positionY = positionY;
	}
	
	
}
