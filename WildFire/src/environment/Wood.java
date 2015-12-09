package environment;

import main.SimulationManager;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.GridPoint;
/**
 * Wood-class which represents a square piece of forest
 * @author carsten
 *
 */
public class Wood implements InformationProvider
{
	// Increases by rain decreases by time and fire in neighborhood fire cannot
	// enter if > 0 (threshold)
	private double wetness;
	// Life points - depends on material
	private double health;
	// Defines material factor, how much water can be stored, how fast it
	// transpires and how hot this material can burn
	private double material;
	private GridPoint position;

	/**
	 * create a piece of forest
	 * @param health
	 * @param material
	 * @param position
	 */
	public Wood(double health, double material, GridPoint position)
	{
		this.wetness = health * material;
		this.health = health;
		this.material = material;
		this.position = position;
	}
	/**
	 * decrease wetness or life points - used by fire
	 * @param decrease
	 * @return
	 */
	public double burn(double decrease)
	{
		if (this.wetness > 0)
		{
			this.wetness -= decrease;
			if (this.wetness < 0)
			{
				this.health += this.wetness;
				this.wetness = 0;
			}
		} else
		{
			this.health -= decrease;
		}
		if (this.health <= 0)
		{
			Context<Object> context = SimulationManager.getContext();
			context.remove(this);
			return 0;
		}
		return this.material * this.health * 0.1 + this.material;
	}

	public double getWetness()
	{
		return wetness;
	}

	public double getHealth()
	{
		return this.health;
	}

	public void shower(double increase)
	{
		this.wetness += increase;
		if (this.wetness > this.health * material)
			this.wetness = this.health * material;
	}

	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE, priority = 993)
	public void transpire()
	{
		this.wetness -= this.wetness * material * 0.1;
	}

	@Override
	public Information getInformation()
	{
		return new WoodInformation(position, health);
	}

	public static class WoodInformation extends Information
	{

		private double health;

		private WoodInformation(GridPoint position, double health)
		{
			super(position);
			this.health = health;
		}

		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public WoodInformation(GridPoint position)
		{
			super(position, true);
		}

		public double getHealth()
		{
			return health;
		}
	}
}
