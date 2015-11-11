package agent;

import repast.simphony.engine.environment.RunEnvironment;

public abstract class Information {
	private double timestamp;
	private Integer positionX;
	private Integer positionY;
	
	public Information(Integer positionX, Integer positionY) {
		this.timestamp = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		this.positionX = positionX;
		this.positionY = positionY;
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
	
	public boolean isNewerInformation(Information otherInformation)
	{
		return timestamp > otherInformation.getTimestamp();
	}
}
