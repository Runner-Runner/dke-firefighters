package environment;

import main.CommonKnowledge;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

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
	
	public CloudFactory(Context<Object> context, Wind wind, double cloudFrequency, int minCloudDim, int maxCloudDim, int forestDim, double maxRain, double minRain, double maxTank, double minTank)
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
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE*CommonKnowledge.CLOUD_FACTOR, priority = 998)
	public void createCloud() 
	{
		//if new cloud is going to be created depends on actual wind speed, cloud frequency and random value
		if(RandomHelper.nextDouble()<cloudFrequency*wind.getSpeed()){
			double x = RandomHelper.nextDouble() * forestDim;
			double y = RandomHelper.nextDouble() * forestDim;
//			int dim = forestDim+2*maxCloudDim;
//			double wD = this.wind.getWindDirection();
//			if(wD>Math.PI/4 && wD<=0.75*Math.PI)
//			{
//				x = RandomHelper.nextIntFromTo(0,dim-1);
//				y = 0;
//			}
//			else if(wD > 0.75*Math.PI && wD<=1.25*Math.PI)
//			{
//				x = dim-1;
//				y = RandomHelper.nextIntFromTo(0,dim-1);
//			}
//			else if(wD > 1.25*Math.PI && wD<=1.75*Math.PI)
//			{
//				x = RandomHelper.nextIntFromTo(0,dim-1);
//				y = dim-1;
//			}
//			else
//			{
//				x = 0;
//				y = RandomHelper.nextIntFromTo(0,dim-1);
//			}
			double tank = minTank+RandomHelper.nextDouble()*(maxTank-minTank);
			Cloud newCloud = new Cloud(context, wind, tank, maxRain, minRain);
			context.add(newCloud);
			CommonKnowledge.getSpace().moveTo(newCloud, x,y);
			CommonKnowledge.getGrid().moveTo(newCloud, (int)x,(int)y);
			newCloud.init(minCloudDim,maxCloudDim);
		}	
	}
}
