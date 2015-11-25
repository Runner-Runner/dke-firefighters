package environment;

import java.awt.Point;
import java.util.ArrayList;

import main.CommonKnowledge;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;


public class Cloud implements InformationProvider 
{
	//Reference to global wind
	private Wind wind;	
	//Water for each cell this cloud carries
	private double tank;	
	//Number of water which rains in each iteration
	private double rain;	
	//Max rain value
	private double maxRain;	
	//Min rain value
	private double minRain;
	private Context<Object> context;
	//Cells which are not at the map yet
	private ArrayList<Point> futureCells;
	//Inertia of this cloud
	private double inertia;
	
	public Cloud(Context<Object> context, Wind wind, double tank, double maxRain, double minRain) 
	{
		super();
		this.context = context;
		this.wind = wind;
		this.tank = tank;
		this.rain = minRain+RandomHelper.nextDouble()*(maxRain-minRain);
		this.maxRain = maxRain;
		this.minRain = minRain;
		this.futureCells = new ArrayList<Point>();
		this.inertia = 0.6; //TODO maybe depend on tank
	}
	
	public void init(int minDim, int maxDim)
	{
		int xDim = RandomHelper.nextIntFromTo(0,maxDim-minDim)+minDim;
		int yDim = RandomHelper.nextIntFromTo(0,maxDim-minDim)+minDim;
		//generate futureCells 
		for(int i =-xDim/2;i<=xDim/2;i++)
		{
			for(int j = -yDim/2;j<yDim/2;j++)
				futureCells.add(new Point(j,i));
		}
	}
	
	
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 994)
	public void step() 
	{
		if(move())
		{
			addCells();
		}
		changePower();
	}
	
	//called by WaterCells
	public double getRain()
	{
		if(tank>=rain)
		{
			tank-=rain;
			return rain;
		}
		else
		{
			return 0;
		}
	}
	
	public double getInertia() 
	{
		return inertia;
	}

	//Add cells to map
	private void addCells() 
	{
		NdPoint myPoint = CommonKnowledge.getSpace().getLocation(this);
		double x = myPoint.getX();
		double y = myPoint.getY();
		for(int i =0;i<futureCells.size();i++)
		{
			Point p = futureCells.get(i);
			if(onMap(x+p.x, y+p.y))
			{
				WaterCell wc = new WaterCell(wind, this);
				context.add(wc);
				CommonKnowledge.getSpace().moveTo(wc, x+p.x, y+p.y);
				CommonKnowledge.getGrid().moveTo(wc, (int)(x+p.x), (int)(y+p.y));
				futureCells.remove(p);
				i--;
			}
		}
	}
	
	//Change power of rain 
	private void changePower()
	{
		this.rain+=RandomHelper.createNormal(0,1).nextDouble();
		if(this.rain>maxRain)
			this.rain = maxRain;
		else if (this.rain < minRain)
			this.rain = minRain;
	}
	
	//Move according wind direction/speed
	private boolean move() 
	{
		//Get actual point
		NdPoint myPoint = CommonKnowledge.getSpace().getLocation(this);
		//Get angle from wind
		double angle = this.wind.getWindDirection();
		//Cloud is a little bit slower than wind
		double distance = this.inertia * this.wind.getSpeed();
		//Look if new position is on map
		double newX = myPoint.getX() + Math.cos(angle) * distance;
		double newY = myPoint.getY() + Math.sin(angle) * distance;
		if (!onMap(newX, newY)) 
		{
			Context<Object> context = CommonKnowledge.getContext();
			context.remove(this);
			return false;
		} 
		else 
		{
			//Move in continuous space
			CommonKnowledge.getSpace().moveByVector(this, distance, angle, 0);
			myPoint = CommonKnowledge.getSpace().getLocation(this);
			//Move in grid
			CommonKnowledge.getGrid().moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			return true;
		}
	}
	
	private boolean onMap(double x, double y)
	{
		return x >= 0 && y >= 0 && x < CommonKnowledge.getGrid().getDimensions().getWidth()
				&& y < CommonKnowledge.getGrid().getDimensions().getHeight();
	}

	@Override
	public CloudInformation getInformation() 
	{
		return new CloudInformation(CommonKnowledge.getGrid().getLocation(this), rain);
	}
	
	public static class CloudInformation extends Information 
	{

		private double rain;
		
		private CloudInformation(GridPoint position, double rain) 
		{
			super(position);
			this.rain = rain;
		}

		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public CloudInformation(GridPoint position)
		{
			super(position, true);
		}
		
		public double getRain() 
		{
			return rain;
		}
	}
}
