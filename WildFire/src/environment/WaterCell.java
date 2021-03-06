package environment;

import main.SimulationManager;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
/**
 * A water cell is one little cell and belongs to a "mother-cloud"
 * @author carsten
 *
 */
public class WaterCell
{
	private Wind wind; 
	private Cloud cloud;
	/**
	 * create a watercell
	 * @param wind
	 * @param cloud
	 */
	public WaterCell(Wind wind, Cloud cloud)
	{
		super();
		this.wind = wind;
		this.cloud = cloud;
	}

	
	//drop water on current cell
	private void rain()
	{
		double rain = this.cloud.getRain();
		// No water in cloud any more
		if (rain == 0)
		{
			Context<Object> context = SimulationManager.getContext();
			context.remove(this);
		} else
		{
			Fire fire = null;
			Wood wood = null;
			Grid<Object> grid = SimulationManager.getGrid();
			GridPoint current = grid.getLocation(this);
			for (Object o : grid.getObjectsAt(current.getX(), current.getY()))
			{
				if (o instanceof Fire)
				{
					fire = (Fire) o;
					break;
				} else if (o instanceof Wood)
				{
					wood = (Wood) o;
				}
			}
			//extinguish fire in this cell
			if (fire != null)
			{
				fire.decreaseHeat(rain, false);
			} 
			//or wet the wood in this cell
			else if (wood != null)
			{
				wood.shower(rain);
			}
		}
	}

	// Move according wind direction/speed
	private boolean move()
	{
		ContinuousSpace<Object> space = SimulationManager.getSpace();
		Grid<Object> grid = SimulationManager.getGrid();
		// Get actual point
		NdPoint myPoint = space.getLocation(this);
		// Get angle from wind
		double angle = this.wind.getWindDirection();
		// Cloud is a little bit slower than wind
		double distance = this.cloud.getInertia() * this.wind.getSpeed();
		// Look if new position is on map
		double newX = myPoint.getX() + Math.cos(angle) * distance;
		double newY = myPoint.getY() + Math.sin(angle) * distance;
		if (newX < 0 || newY < 0 || newX >= grid.getDimensions().getWidth()
				|| newY >= grid.getDimensions().getHeight())
		{
			Context<Object> context = SimulationManager.getContext();
			context.remove(this);
			return false;

		} else
		{
			// Move in continuous space
			space.moveByVector(this, distance, angle, 0);
			myPoint = space.getLocation(this);
			// Move in grid
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			return true;
		}
	}
	
	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE, priority = 995)
	public void step()
	{
		if (move())
			rain();
	}
}
