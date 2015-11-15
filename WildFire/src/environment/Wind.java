package environment;

import java.util.Random;

import agent.Information;
import agent.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
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
	
	@ScheduledMethod(start = 1, interval = 3, priority = 1)
	public void step() {
		changeDirection();
		changeSpeed();
		System.out.println("Wind: direction:"+getWindDirectionDegree()+" speed: "+speed);
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
		return new WindInformation(null, null, speed, windDirection);
	}
	
	public class WindInformation extends Information {

		private double speed;
		private double windDirection;
		
		private WindInformation(Integer positionX, Integer positionY, double speed, double windDirection) {
			super(positionX, positionY);
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
