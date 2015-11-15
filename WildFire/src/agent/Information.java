package agent;

import repast.simphony.engine.environment.RunEnvironment;

public abstract class Information {
	private double timestamp;
	private Integer positionX;
	private Integer positionY;
	
	/**
	 * If set true, this class is to be removed from its position in the knowledge mapping 
	 * (e.g. because the fire was extinguished etc.).
	 */
	private boolean emptyInstance;
	
	public Information(Integer positionX, Integer positionY)
	{
		this(positionX, positionY, false);
	}
	
	public Information(Integer positionX, Integer positionY, boolean removeInstance) {
		this.timestamp = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		this.positionX = positionX;
		this.positionY = positionY;
		this.emptyInstance = removeInstance;
	}

	public double getTimestamp() {
		return timestamp;
	}

	public Integer getPositionX() {
		return positionX;
	}

	public Integer getPositionY() {
		return positionY;
	}
	
	public boolean isEmptyInstance() {
		return emptyInstance;
	}
	
	public boolean isNewerInformation(Information otherInformation)
	{
		if(otherInformation == null)
		{
			return true;
		}
		return timestamp > otherInformation.getTimestamp();
	}
}
