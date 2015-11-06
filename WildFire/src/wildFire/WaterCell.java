package wildFire;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class WaterCell {
	private ContinuousSpace<Object> space; // actual exact position in terrain
	private Grid<Object> grid; // actual cell in grid
	private Wind wind; // reference to global wind
	private Cloud cloud;
	
	
	public WaterCell(ContinuousSpace<Object> space, Grid<Object> grid,
			Wind wind, Cloud cloud) {
		super();
		this.space = space;
		this.grid = grid;
		this.wind = wind;
		this.cloud = cloud;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step(){
		if(move())
			rain();
	}
	
	private void rain(){
		double rain = this.cloud.getRain();
		//no water in cloud any more
		if(rain == 0){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
		}
		else{
			Fire fire = null;
			Wood wood = null;
			GridPoint current = grid.getLocation(this);
			for(Object o : grid.getObjectsAt(current.getX(), current.getY())){
				if(o instanceof Fire){
					fire = (Fire)o;
					break;
				}
				else if(o instanceof Wood){
					wood = (Wood)o;
				}
			}
			if(fire!=null){				
				fire.decreaseHeat(rain);
			}
			else if(wood!=null){
				wood.shower(rain);
			}
		}
	}
	// move according wind direction/speed
	private boolean move() {
		// get actual point
		NdPoint myPoint = space.getLocation(this);
		// get angle from wind
		double angle = this.wind.getWindDirectionRadians();
		// cloud is a little bit slower than wind
		double distance = this.cloud.getInertia() * this.wind.getSpeed();
		// look if new position is on map
		double newX = myPoint.getX() + Math.cos(angle) * distance;
		double newY = myPoint.getY() + Math.sin(angle) * distance;
		if (newX < 0 || newY < 0 || newX >= grid.getDimensions().getWidth()
				|| newY >= grid.getDimensions().getHeight()) {
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
			return false;

		} else {
			// move in continuous space
			space.moveByVector(this, distance, angle, 0);
			myPoint = space.getLocation(this);
			// move in grid
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			return true;
		}
	}
}
