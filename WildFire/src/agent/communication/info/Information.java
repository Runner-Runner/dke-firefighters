package agent.communication.info;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;

public abstract class Information
{
	private double timestamp;
	private GridPoint position;

	/**
	 * If set true, this class is to be removed from its position in the belief
	 * mapping (e.g. because the fire was extinguished etc.).
	 */
	private boolean emptyInstance;

	public Information(GridPoint position)
	{
		this(position, false);
	}

	public Information(GridPoint position, boolean removeInstance)
	{
		this.timestamp = RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
		this.position = position;
		this.emptyInstance = removeInstance;
	}

	public double getTimestamp()
	{
		return timestamp;
	}

	public GridPoint getPosition()
	{
		return this.position;
	}

	public boolean isEmptyInstance()
	{
		return emptyInstance;
	}

	public boolean isNewerInformation(Information otherInformation)
	{
		if (otherInformation == null)
		{
			return true;
		}
		return timestamp > otherInformation.getTimestamp();
	}
}
