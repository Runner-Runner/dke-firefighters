package environment;

import java.awt.Point;
import java.util.ArrayList;

import main.SimulationManager;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
/**
 * This class represents a cloud, which moves with respect to wind-speed and direction.
 * It carries a defined amount of water within its "tank" and specifies the amount of water which
 * rains in each iteration.
 * The shape consists of multiple watercells
 * 
 * @author carsten
 *
 */
public class Cloud implements InformationProvider
{
	// Reference to global wind
	private Wind wind;
	// Water for each cell this cloud carries
	private double tank;
	// Number of water which rains in each iteration
	private double rain;
	// Max rain value
	private double maxRain;
	// Min rain value
	private double minRain;
	// Cells which are not at the map yet
	private ArrayList<Point> futureCells;
	// Inertia of this cloud (not as fast as the wind)
	private double inertia;
	
	private Context<Object> context;
	
	/**
	 * create a cloud
	 * @param context needs a context to add its watercell-childs
	 * @param wind 
	 * @param tank amount of water
	 * @param maxRain maximum amount of rain in an iteration
	 * @param minRain minimum amount of rain in an iteration
	 */
	public Cloud(Context<Object> context, Wind wind, double tank,
			double maxRain, double minRain)
	{
		super();
		this.context = context;
		this.wind = wind;
		this.tank = tank;
		this.rain = minRain + RandomHelper.nextDouble() * (maxRain - minRain);
		this.maxRain = maxRain;
		this.minRain = minRain;
		this.futureCells = new ArrayList<Point>();
		// may depend on the amount of water (future work)
		this.inertia = 0.6; 
	}
	/**
	 * initialize the cloud
	 * @param minDim minimum dimension of cloud
	 * @param maxDim maximum dimension of cloud
	 */
	public void init(int minDim, int maxDim)
	{
		int xDim = RandomHelper.nextIntFromTo(0, maxDim - minDim - 1) + minDim;
		int yDim = RandomHelper.nextIntFromTo(0, maxDim - minDim - 1) + minDim;
		// generate water-cells, which will be added later on (may not placed on map)
		for (int i = -xDim / 2; i <= xDim / 2; i++)
		{
			for (int j = -yDim / 2; j < yDim / 2; j++)
				futureCells.add(new Point(j, i));
		}
	}

	

	/**
	 * tank will decrease.
	 * called by water-cells which ask for water to rain
	 * @return
	 */
	public double getRain()
	{
		if (tank >= rain)
		{
			tank -= rain;
			return rain;
		} else
		{
			return 0;
		}
	}
	
	public double getInertia()
	{
		return inertia;
	}

	// Add cells to map
	private void addCells()
	{
		NdPoint myPoint = SimulationManager.getSpace().getLocation(this);
		double x = myPoint.getX();
		double y = myPoint.getY();
		for (int i = 0; i < futureCells.size(); i++)
		{
			Point p = futureCells.get(i);
			if (onMap(x + p.x, y + p.y))
			{
				WaterCell wc = new WaterCell(wind, this);
				context.add(wc);
				SimulationManager.getSpace().moveTo(wc, x + p.x, y + p.y);
				SimulationManager.getGrid().moveTo(wc, (int) (x + p.x),
						(int) (y + p.y));
				futureCells.remove(p);
				i--;
			}
		}
	}

	// Change power of rain (Gaussian distributed)
	private void changePower()
	{
		this.rain += RandomHelper.createNormal(0, 1).nextDouble();
		if (this.rain > maxRain)
			this.rain = maxRain;
		else if (this.rain < minRain)
			this.rain = minRain;
	}

	// Move according wind direction/speed
	private boolean move()
	{
		// Get actual point
		NdPoint myPoint = SimulationManager.getSpace().getLocation(this);
		// Get angle from wind
		double angle = this.wind.getWindDirection();
		// Cloud is a little bit slower than wind
		double distance = this.inertia * this.wind.getSpeed();
		// Look if new position is on map
		double newX = myPoint.getX() + Math.cos(angle) * distance;
		double newY = myPoint.getY() + Math.sin(angle) * distance;
		if (!onMap(newX, newY))
		{
			Context<Object> context = SimulationManager.getContext();
			context.remove(this);
			return false;
		} else
		{
			// Move in continuous space
			SimulationManager.getSpace().moveByVector(this, distance, angle, 0);
			myPoint = SimulationManager.getSpace().getLocation(this);
			// Move in grid
			SimulationManager.getGrid().moveTo(this, (int) myPoint.getX(),
					(int) myPoint.getY());
			return true;
		}
	}
	
	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE, priority = 994)
	public void step()
	{
		if (move())
		{
			addCells();
		}
		changePower();
	}
	//checks if coordinates are on map
	private boolean onMap(double x, double y)
	{
		return x >= 0 && y >= 0
				&& x < SimulationManager.getGrid().getDimensions().getWidth()
				&& y < SimulationManager.getGrid().getDimensions().getHeight();
	}

	@Override
	public CloudInformation getInformation()
	{
		return new CloudInformation(SimulationManager.getGrid().getLocation(
				this), rain);
	}
	/**
	 * Information-Class which is used by agents do keep clouds in mind
	 * @author carsten
	 *
	 */
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
