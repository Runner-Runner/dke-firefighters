package environment;

import main.SimulationManager;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
/**
 * CloudFactory, which creates arbitrarily creates clouds on map
 * @author carsten
 *
 */
public class CloudFactory
{
	private Wind wind;
	private Context<Object> context;
	private double cloudFrequency;
	private int maxCloudDim;
	private int minCloudDim;
	private int forestDim;
	private double maxTank;
	private double minTank;
	private double maxRain;
	private double minRain;

	/**
	 * create the cloudFactory
	 * @param context context to add clouds to
	 * @param wind	clouds appear with respect to current wind speed
	 * @param cloudFrequency defines how often clouds appear
	 * @param minCloudDim
	 * @param maxCloudDim
	 * @param forestDim
	 * @param maxRain
	 * @param minRain
	 * @param maxTank
	 * @param minTank
	 */
	public CloudFactory(Context<Object> context, Wind wind,
			double cloudFrequency, int minCloudDim, int maxCloudDim,
			int forestDim, double maxRain, double minRain, double maxTank,
			double minTank)
	{
		this.context = context;
		this.wind = wind;
		this.cloudFrequency = cloudFrequency;
		this.maxCloudDim = maxCloudDim;
		this.minCloudDim = minCloudDim;
		this.forestDim = forestDim;
		this.maxRain = maxRain;
		this.minRain = minRain;
		this.maxTank = maxTank;
		this.minTank = minTank;
	}

	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE
			* SimulationManager.CLOUD_FACTOR, priority = 998)
	public void createCloud()
	{
		// if new cloud is going to be created depends on actual wind speed,
		// cloud frequency and random value
		if (RandomHelper.nextDouble() < cloudFrequency * wind.getSpeed())
		{
			double x = RandomHelper.nextDouble() * forestDim;
			double y = RandomHelper.nextDouble() * forestDim;

			double tank = minTank + RandomHelper.nextDouble()
					* (maxTank - minTank);
			Cloud newCloud = new Cloud(context, wind, tank, maxRain, minRain);
			context.add(newCloud);
			SimulationManager.getSpace().moveTo(newCloud, x, y);
			SimulationManager.getGrid().moveTo(newCloud, (int) x, (int) y);
			newCloud.init(minCloudDim, maxCloudDim);
		}
	}
}
