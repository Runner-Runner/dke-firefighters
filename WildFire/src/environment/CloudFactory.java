package environment;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class CloudFactory {
	private Wind wind;
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	private Context<Object> context;
	private Random random;
	private double cloudFrequency;
	private int maxCloudDim;
	private int minCloudDim;
	private int forestDim;
	private double maxTank;
	private double minTank;
	private double maxRain;
	private double minRain;
	
	public CloudFactory(Context<Object> context, Wind wind, ContinuousSpace<Object> space, Grid<Object> grid, double cloudFrequency, int minCloudDim, int maxCloudDim, int forestDim, double maxRain, double minRain, double maxTank, double minTank){
		this.context = context;
		this.wind = wind;
		this.space = space;
		this.grid = grid;
		this.random = new Random();
		this.cloudFrequency = cloudFrequency;
		this.maxCloudDim = maxCloudDim;
		this.minCloudDim = minCloudDim;
		this.forestDim = forestDim;
		this.maxRain = maxRain;
		this.minRain = minRain;
		this.maxTank = maxTank;
		this.minTank = minTank;
	}
	
	
	@ScheduledMethod(start = 1, interval = 10, priority = 998)
	public void createCloud() {
		//if new cloud is going to be created depends on actual wind speed, cloud frequency and random value
		if(random.nextDouble()<cloudFrequency*wind.getSpeed()){
			int x;
			int y;
			int dim = forestDim+2*maxCloudDim;
			double wD = this.wind.getWindDirection();
			if(wD>Math.PI/4 && wD<=0.75*Math.PI){
				x = random.nextInt(dim);
				y = 0;
			}
			else if(wD > 0.75*Math.PI && wD<=1.25*Math.PI){
				x = dim-1;
				y = random.nextInt(dim);
			}
			else if(wD > 1.25*Math.PI && wD<=1.75*Math.PI){
				x = random.nextInt(dim);
				y = dim-1;
			}
			else{
				x = 0;
				y = random.nextInt(dim);
			}
			double tank = minTank+random.nextDouble()*(maxTank-minTank);
			Cloud newCloud = new Cloud(context, wind, space, grid, tank, maxRain, minRain);
			context.add(newCloud);
			space.moveTo(newCloud, x,y);
			grid.moveTo(newCloud, x,y);
			newCloud.init(minCloudDim,maxCloudDim);
		}	
	}
}
