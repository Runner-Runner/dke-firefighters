package wildFire;

import java.util.Random;

import repast.simphony.engine.schedule.ScheduledMethod;

public class Wind {
	private Random random;
	private double windDirection;	//in degree
	private double speed;		//in distance per step
	private double maxSpeed;
	
	public Wind(double maxSpeed){
		this.random = new Random();
		this.maxSpeed = maxSpeed;
		this.speed = random.nextDouble()*maxSpeed;
		this.windDirection = random.nextDouble()*360;
	}
	
	@ScheduledMethod(start = 1, interval = 3)
	public void step() {
		changeDirection();
		changeSpeed();
		System.out.println("Wind: direction:"+windDirection+" speed: "+speed);
	}
	
	public double getWindDirectionDegree() {
		return this.windDirection;
	}

	public double getWindDirectionRadians() {
		return windDirection*Math.PI/180;
	}

	public double getSpeed() {
		return speed;
	}



	private void changeDirection(){
		this.windDirection += random.nextGaussian()*90/6;
		this.windDirection%=360;
	}
	
	private void changeSpeed(){
		this.speed += random.nextGaussian()*maxSpeed/6;
		if(this.speed<0)
			this.speed = 0;
		else if(this.speed>maxSpeed)
			this.speed = maxSpeed;
	}
	
	
	
}
