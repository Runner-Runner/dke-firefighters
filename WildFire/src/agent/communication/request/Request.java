package agent.communication.request;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;

/**
 * Provides a message type requesting a certain type of help from other agents.
 */
public abstract class Request
{
	protected int id;
	protected int importance;
	protected GridPoint position;
	protected String senderID;
	protected double timestamp;

	private static int requestCounter = 0;

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
