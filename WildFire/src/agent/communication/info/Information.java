package agent.communication.info;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;

/**
 * Provides an information about a certain entity of the simulation (e.g. fires
 * or other foresters) at a certain position that is part of the belief of an
 * agent and therefore does not need to be still correct (the information could
 * be obsolete by now).
 * <p>
 * Information can also represent the absence of such a given entity at a
 * certain position, to overwrite obsolete information in the belief. Such an
 * information is called an "empty instance".
 */
public abstract class Information
{
	/**
	 * The time step in which the information was perceived.
	 */
	private double timestamp;

	/**
	 * Position at which the referenced entity is located (optional; e.g. wind
	 * is global)
	 */
	private GridPoint position;

	/**
	 * If set true, this entity class is to be removed from its position in the
	 * belief mapping (e.g. because the fire was extinguished etc.).
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

	/**
	 * @param otherInformation
	 * @return Whether this information was perceived more recently than the
	 *         other information and thereby is supposed to be more accurate.
	 */
	public boolean isNewerInformation(Information otherInformation)
	{
		if (otherInformation == null)
		{
			return true;
		}
		return timestamp > otherInformation.getTimestamp();
	}
}
