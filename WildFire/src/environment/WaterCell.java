package environment;

import main.CommonKnowledge;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class WaterCell 
{
	private Wind wind; // reference to global wind
	private Cloud cloud;
	
	
	public WaterCell(
			Wind wind, Cloud cloud)
	{
		super();
		this.wind = wind;
		this.cloud = cloud;
	}

	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 995)
	public void step()
	{
		if(move())
			rain();
	}
	
	private void rain()
	{
		double rain = this.cloud.getRain();
		//No water in cloud any more
		if(rain == 0)
		{
			Context<Object> context = CommonKnowledge.getContext();
			context.remove(this);
		}
		else
		{
			Fire fire = null;
			Wood wood = null;
			Grid<Object> grid = CommonKnowledge.getGrid();
			GridPoint current = grid.getLocation(this);
			for(Object o : grid.getObjectsAt(current.getX(), current.getY()))
			{
				if(o instanceof Fire)
				{
					fire = (Fire)o;
					break;
				}
				else if(o instanceof Wood)
				{
					wood = (Wood)o;
				}
			}
			if(fire!=null)
			{				
				fire.decreaseHeat(rain, false);
			}
			else if(wood!=null)
			{
				wood.shower(rain);
			}
		}
	}
	//Move according wind direction/speed
	private boolean move() 
	{
		ContinuousSpace<Object> space = CommonKnowledge.getSpace();
		Grid<Object> grid = CommonKnowledge.getGrid();
		//Get actual point
		NdPoint myPoint = space.getLocation(this);
		//Get angle from wind
		double angle = this.wind.getWindDirection();
		//Cloud is a little bit slower than wind
		double distance = this.cloud.getInertia() * this.wind.getSpeed();
		//Look if new position is on map
		double newX = myPoint.getX() + Math.cos(angle) * distance;
		double newY = myPoint.getY() + Math.sin(angle) * distance;
		if (newX < 0 || newY < 0 || newX >= grid.getDimensions().getWidth()
				|| newY >= grid.getDimensions().getHeight()) 
		{
			Context<Object> context = CommonKnowledge.getContext();
			context.remove(this);
			return false;

		} else 
		{
			//Move in continuous space
			space.moveByVector(this, distance, angle, 0);
			myPoint = space.getLocation(this);
			//Move in grid
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			return true;
		}
	}
}
