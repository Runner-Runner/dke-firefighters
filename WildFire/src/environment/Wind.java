package environment;

import main.SimulationManager;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;
/**
 * Wind class which simulates a global wind with speed and direction
 * which change with respect to a random gaussian value
 * @author carsten
 *
 */
public class Wind implements InformationProvider
{
	//in radians
	private double windDirection;
	private double speed; 
	// in distance per step
	private double maxSpeed;
	/**
	 * create the global wind
	 * @param maxSpeed
	 */
	public Wind(double maxSpeed)
	{
		this.maxSpeed = maxSpeed;
		this.speed = RandomHelper.nextDouble() * maxSpeed;
		this.windDirection = RandomHelper.nextDouble() * 2 * Math.PI;
	}

	

	public double getWindDirection()
	{
		return windDirection;
	}

	public double getSpeed()
	{
		return speed;
	}
	//change direction by gaussian random value
	private void changeDirection()
	{
		this.windDirection += RandomHelper.createNormal(0, 1).nextDouble()
				* Math.PI / 2 / 12;
		this.windDirection %= 2 * Math.PI;
	}
	//change speed by gaussian random value
	private void changeSpeed()
	{
		this.speed += RandomHelper.createNormal(0, 1).nextDouble() * maxSpeed
				/ 12;
		if (this.speed < 0)
			this.speed = 0;
		else if (this.speed > maxSpeed)
			this.speed = maxSpeed;
	}
	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE
			* SimulationManager.WIND_FACTOR, priority = 999)
	public void step()
	{
		changeDirection();
		changeSpeed();
	}

	@Override
	public WindInformation getInformation()
	{
		return new WindInformation(null, speed, windDirection);
	}
	/**
	 * Informationclass which is used by agents to keep windinformation in mind
	 * @author carsten
	 *
	 */
	public class WindInformation extends Information
	{

		private double speed;
		private double windDirection;

		private WindInformation(GridPoint position, double speed,
				double windDirection)
		{
			super(position);
			this.speed = speed;
			this.windDirection = windDirection;
		}

		public double getSpeed()
		{
			return speed;
		}

		public double getWindDirection()
		{
			return windDirection;
		}
	}
}
