package environment;

import main.CommonKnowledge;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;

public class Wind implements InformationProvider {
	private double windDirection;	//in radiant
	private double speed;		//in distance per step
	private double maxSpeed;
	
	public Wind(double maxSpeed){
		this.maxSpeed = maxSpeed;
		this.speed = RandomHelper.nextDouble()*maxSpeed;
		this.windDirection = RandomHelper.nextDouble()*2*Math.PI;
	}
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE*CommonKnowledge.WIND_FACTOR, priority = 999)
	public void step() {
		changeDirection();
		changeSpeed();
	}

	public double getWindDirection() {
		return windDirection;
	}

	public double getSpeed() {
		return speed;
	}



	private void changeDirection(){
		this.windDirection += RandomHelper.createNormal(0,1).nextDouble() *Math.PI/2/12;
		this.windDirection%=2*Math.PI;
	}
	
	private void changeSpeed(){
		this.speed += RandomHelper.createNormal(0,1).nextDouble()*maxSpeed/6;
		if(this.speed<0)
			this.speed = 0;
		else if(this.speed>maxSpeed)
			this.speed = maxSpeed;
	}
	
	@Override
	public WindInformation getInformation()
	{
		return new WindInformation( null, speed, windDirection);
	}
	
	public class WindInformation extends Information {

		private double speed;
		private double windDirection;
		
		private WindInformation(GridPoint position, double speed, double windDirection) {
			super(position);
			this.speed = speed;
			this.windDirection = windDirection;
		}

		public double getSpeed() {
			return speed;
		}

		public double getWindDirection() {
			return windDirection;
		}
	}
}
