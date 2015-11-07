package wildFire;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public abstract class ForesterAgent {
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	
	//in distance per step
	protected double speed;
	//defines the number of time steps this forester experienced burning injuries
	protected int burningTime = 0;
	
	//TODO how to best and easiest model the weather knowledge of a part of the map at a given time step?
	protected Grid<Object> weatherKnowledgeGrid;
	
	//number of time steps it takes until burning injuries of a forester become lethal and he dies
	protected final static int LETHAL_BURNING_TIME = 3;
	
	public ForesterAgent(ContinuousSpace<Object> space, Grid<Object> grid, double speed) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		//check if in burning environment
		if(isOnBurningTile())
		{
			burn();
			return;
		}
		
		decideOnActions();
	}
	
	protected abstract void decideOnActions();
	
	/**
	 * Extinguish fire in one grid space. Time-consuming action: takes up one time step
	 * 
	 * @param pt
	 */
	protected void extinguishFire(GridPoint pt)
	{
		GridPoint position = grid.getLocation(this);
		
		//check if fire position really is in the Moore neighborhood
		if(Math.abs(position.getX()-pt.getX()) > 1 || Math.abs(position.getY()-pt.getY()) > 1)
		{
			//illegal action
			return;
		}
			
		Iterable<Object> objects = grid.getObjectsAt(pt.getX(), pt.getY());
		Fire fire = null;
		for(Object obj : objects)
		{
			if(obj instanceof Fire)
			{
				fire = (Fire)obj;
				break;
			}
		}
		if(fire == null)
		{
			//no fire to extinguish
			return;
		}
		
		//TODO include extinguishing duration?
		Context<Object> context = ContextUtils.getContext(fire);
		context.remove(fire);
	}
	
	/**
	 * Obtain information about the weather conditions in the Moore neighborhood
	 */
	protected void checkWeather()
	{
		//TODO
	}
	
	protected void moveTowards(GridPoint pt) {
		//get actual point
		NdPoint myPoint = space.getLocation(this);
		//cast to continuous point
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		//calculate angle to move
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint);
		//move in continuous space
		space.moveByVector(this, speed, angle, 0);
		myPoint = space.getLocation(this);

		//move in grid
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}

	public boolean isOnBurningTile()
	{
		GridPoint location = grid.getLocation(this);
		Iterable<Object> objects = grid.getObjectsAt(location.getX(), location.getY());
		for(Object obj : objects)
		{
			if(obj instanceof Fire)
			{
				return true;
			}
		}
		return false;
	}
	
	protected void burn()
	{
		burningTime++;
		if(burningTime >= LETHAL_BURNING_TIME)
		{
			//die
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
		}
	}
	
	public enum Behavior {
		COOPERATIVE, SELFISH, MIXED, DESTRUCTIVE
	};
}
