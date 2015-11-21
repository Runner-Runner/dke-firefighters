package agent;

public abstract class Request {
	//TODO maybe use enum for importance
	protected int importance;
	protected Integer positionX;
	protected Integer positionY;
	protected String senderID;
	
	public Request(String senderID, int importance, Integer positionX, Integer positionY) {
		super();
		this.senderID = senderID;
		this.importance = importance;
		this.positionX = positionX;
		this.positionY = positionY;
	}

	public String getSenderID() {
		return senderID;
	}

	public int getImportance() {
		return importance;
	}

	public Integer getPositionX() {
		return positionX;
	}

	public Integer getPositionY() {
		return positionY;
	}
	
	
	
	
}
