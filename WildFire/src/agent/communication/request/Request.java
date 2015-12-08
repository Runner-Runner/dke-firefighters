package agent.communication.request;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;

public abstract class Request
{
	private static int requestCounter = 0;
	// TODO maybe use enum for importance
	protected int importance;
	protected GridPoint position;
	protected String senderID;
	protected int id;
	protected double timestamp;

	public Request(String senderID, int importance, GridPoint position)
	{
		super();
		this.senderID = senderID;
		this.importance = importance;
		this.position = position;
		this.id = requestCounter++;
		this.timestamp = RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
	}

	public int getId()
	{
		return id;
	}

	public double getTimestamp()
	{
		return timestamp;
	}

	public String getSenderID()
	{
		return senderID;
	}

	public int getImportance()
	{
		return importance;
	}

	public GridPoint getPosition()
	{
		return this.position;
	}

}
