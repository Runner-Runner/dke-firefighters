package environment;

import java.util.Random;

import main.CommonKnowledge;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Wind implements InformationProvider {
	private Random random;
	private double windDirection;	//in radiant
	private double speed;		//in distance per step
	private double maxSpeed;
	
	public Wind(double maxSpeed){
		this.random = new Random();
		this.maxSpeed = maxSpeed;
		this.speed = random.nextDouble()*maxSpeed;
		this.windDirection = random.nextDouble()*2*Math.PI;
	}
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE*CommonKnowledge.WIND_FACTOR, priority = 999)
	public void step() {
		changeDirection();
		changeSpeed();
	}
	
	private double getWindDirectionDegree() {
		return this.windDirection*180/Math.PI;
	}

	public double getWindDirection() {
		return windDirection;
	}

	public double getSpeed() {
		return speed;
	}



	private void changeDirection(){
		this.windDirection += random.nextGaussian()*Math.PI/2/6;
		this.windDirection%=2*Math.PI;
	}
	
	private void changeSpeed(){
		this.speed += random.nextGaussian()*maxSpeed/6;
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
