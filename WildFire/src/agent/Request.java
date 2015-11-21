package agent;

public abstract class Request {
	private static int requestCounter = 0;
	//TODO maybe use enum for importance
	protected int importance;
	protected Integer positionX;
	protected Integer positionY;
	protected String senderID;
	protected int id;
	
	public Request(String senderID, int importance, Integer positionX, Integer positionY) {
		super();
		this.senderID = senderID;
		this.importance = importance;
		this.positionX = positionX;
		this.positionY = positionY;
		this.id = requestCounter++;
	}

	public int getId() {
		return id;
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
