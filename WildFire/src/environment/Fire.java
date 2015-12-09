package environment;

import java.util.List;

import main.SimulationManager;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import statistics.Statistic;

public class Fire implements InformationProvider
{
	// number of points to decrease woods lifepoints each iteration
	private double heat;
	// fire spreads in winds direction
	private Wind wind; 

	private boolean beingExtinguished;
	private ExtinguishMarker extinguishingMarker;
	
	public Fire(double heat, Wind wind)
	{
		super();
		this.heat = heat;
		this.wind = wind;
		Statistic.getInstance().incrementFireCount();
	}

	public double getHeat()
	{
		return this.heat;
	}

	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE, priority = 996)
	public void step()
	{
		//Remove marker if the fire is not getting extinguished anymore
		if(!beingExtinguished && extinguishingMarker != null)
		{
			SimulationManager.getContext().remove(extinguishingMarker);
			extinguishingMarker = null;
		}
		beingExtinguished = false;
		
		Grid<Object> grid = SimulationManager.getGrid();
		GridPoint pt = grid.getLocation(this);
		// Propagation
		GridCellNgh<Wood> nghCreator = new GridCellNgh<Wood>(grid, pt,
				Wood.class, 1, 1);
		List<GridCell<Wood>> gridCells = nghCreator.getNeighborhood(true);
		double windX = Math.cos(this.wind.getWindDirection()) * wind.getSpeed();
		double windY = Math.sin(this.wind.getWindDirection()) * wind.getSpeed();
		for (GridCell<Wood> cell : gridCells)
		{
			double xDiff = cell.getPoint().getX() - pt.getX();
			double yDiff = cell.getPoint().getY() - pt.getY();
			if (cell.items().iterator().hasNext() && windX * xDiff >= 0
					&& windY * yDiff >= 0)
			{
				// In wind direction
				Wood w = cell.items().iterator().next();
				// Decrease wetness
				if (w.getWetness() > 0)
				{
					w.burn(wind.getSpeed() * heat); // TODO take length of
													// windvector into
													// consideration
				}
				// Increase fire or spread fire
				else
				{
					Fire fire = null;
					for (Object o : grid.getObjectsAt(cell.getPoint().getX(),
							cell.getPoint().getY()))
					{
						if (o instanceof Fire)
						{
							fire = (Fire) o;
							break;
						}
					}
					if (fire == null)
					{
						if (RandomHelper.nextDouble() < wind.getSpeed() * 0.1)
						{// TODO take length of windvector into consideration
							Fire f = new Fire(wind.getSpeed() * 0.05
									* this.heat, this.wind);
							SimulationManager.getContext().add(f);
							grid.moveTo(f, cell.getPoint().getX(), cell
									.getPoint().getY());
							SimulationManager.getSpace().moveTo(f,
									cell.getPoint().getX() + 0.5,
									cell.getPoint().getY() + 0.5);
						}
					} else
					{
						fire.increaseHeat(wind.getSpeed() * 0.01 * this.heat);// TODO
																				// take
																				// length
																				// of
																				// windvector
																				// into
																				// consideration
					}
				}
			}
		}
		// burn actual
		Wood material = null;
		for (Object o : grid.getObjectsAt(pt.getX(), pt.getY()))
		{
			if (o instanceof Wood)
			{
				material = (Wood) o;
				break;
			}
		}
		// no material -> fire dies
		if (material == null)
		{
			die();
		} else
		{
			double maxHeat = material.burn(heat);
			this.heat += (maxHeat - this.heat) * 0.05 * wind.getSpeed();
		}
	}

	public void increaseHeat(double add)
	{
		this.heat += add;
	}

	/**
	 * @param sub
	 * @return The amount of heat that was actually decreased.
	 */
	public double decreaseHeat(double sub, boolean byForester)
	{
		this.heat -= sub;

		Context<Object> context = SimulationManager.getContext();
		if (this.heat <= 0)
		{
			die();

			if (byForester)
			{
				Statistic.getInstance().incrementExtinguishedFireCount();
			}
			return sub + this.heat;
		}
		else
		{
			beingExtinguished = true;
			if(extinguishingMarker == null)
			{
				//Add visual marker
				Grid<Object> grid = SimulationManager.getGrid();
				ContinuousSpace<Object> space = SimulationManager.getSpace();
				GridPoint pt = SimulationManager.getGrid().getLocation(this);
				extinguishingMarker = new ExtinguishMarker();
				context.add(extinguishingMarker);
				grid.moveTo(extinguishingMarker, pt.getX(), pt.getY());
				space.moveTo(extinguishingMarker, pt.getX() + 0.5, pt.getY() + 0.5);
			}
		}
		return sub;
	}

	private void die()
	{
		Context<Object> context = SimulationManager.getContext();
		context.remove(this);
		if(extinguishingMarker != null)
		{
			context.remove(extinguishingMarker);
		}
	}
	
	@Override
	public FireInformation getInformation()
	{
		return new FireInformation(SimulationManager.getGrid()
				.getLocation(this), heat);
	}

	public static class FireInformation extends Information
	{

		private double heat;

		public FireInformation(GridPoint position, double heat)
		{
			super(position);
			this.heat = heat;
		}

		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public FireInformation(GridPoint position)
		{
			super(position, true);
		}

		public double getHeat()
		{
			return heat;
		}
	}
	
	public static class ExtinguishMarker
	{
		//Marker solely for UI to visualize fire that is being 
		//extinguished differently in the simulation.
	}
}
