package wildFire;

import java.util.Random;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class StupidAgent {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	public StupidAgent(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		
		Random random = new Random();
		GridPoint gp = new GridPoint(random.nextInt(grid.getDimensions().getWidth()), random.nextInt(grid.getDimensions().getHeight()));
		
		moveTowards(gp);
	}
	public void moveTowards(GridPoint pt) {
		//get actual point
		NdPoint myPoint = space.getLocation(this);
		//cast to continuous point
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		//calculate angle to move
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint);
		//move in continuous space
		space.moveByVector(this, 1, angle, 0);
		myPoint = space.getLocation(this);

		//move in grid
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}
	
}
