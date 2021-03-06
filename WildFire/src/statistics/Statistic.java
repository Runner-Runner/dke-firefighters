package statistics;

import main.SimulationManager;
import agent.ForesterAgent;
import repast.simphony.context.Context;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;
import environment.Fire;
import environment.Wood;

/**
 * Stores evaluation data of a simulation run and makes certain calculations for convenience, e.g. to percent values.
 * 
 * @author Daniel
 *
 */
public class Statistic
{
	private double totalWoodHealth;
	private double totalAgentCount;
	private int totalFireCount;
	private int extinguishedFireCount;
	private int gridWidth;
	private int gridHeight;
	private long[][] covering;

	private static Statistic statistic;

	private Statistic()
	{
		// singleton
	}

	public static Statistic getInstance()
	{
		if (statistic == null)
		{
			statistic = new Statistic();
		}
		return statistic;
	}

	public double getTotalAgentCount()
	{
		return totalAgentCount;
	}

	public int getGridWidth()
	{
		return gridWidth;
	}

	public int getGridHeight()
	{
		return gridHeight;
	}

	public GridPoint getCenter()
	{
		return new GridPoint(gridWidth / 2, gridHeight / 2);
	}

	public void setGridSize(int gridWidth, int gridHeight)
	{
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.covering = new long[gridHeight][gridWidth];
	}
	
	public void tileObserved(int row, int column)
	{
		covering[row][column]++;
	}
	
	public void setTotalWoodHealth(double totalWoodHealth)
	{
		this.totalWoodHealth = totalWoodHealth;
	}

	public void setTotalAgentCount(double totalAgentCount)
	{
		this.totalAgentCount = totalAgentCount;
	}

	public void incrementFireCount()
	{
		totalFireCount++;
	}

	public void incrementExtinguishedFireCount()
	{
		extinguishedFireCount++;
	}

	public double getFireCountPercent()
	{
		Context<Object> context = SimulationManager.getContext();
		IndexedIterable<Object> woodObjects = context.getObjects(Wood.class);
		int numberOfWood = woodObjects.size();
		IndexedIterable<Object> fireObjects = context.getObjects(Fire.class);
		double numberOfFires = fireObjects.size();
		double firePercent = numberOfFires / numberOfWood;
		return firePercent;
	}

	public double getWoodCountPercent()
	{
		return getWoodHealthPercent();
	}
	public long[][] getCovering(){
		return covering;
	}
	public double getWoodHealthPercent()
	{
		if (totalWoodHealth == 0)
		{
			return 0;
		}
		Context<Object> context = SimulationManager.getContext();
		IndexedIterable<Object> objects = context.getObjects(Wood.class);
		double currentTotalWoodHealth = 0;
		for (Object object : objects)
		{
			if (object instanceof Wood)
			{
				Wood wood = (Wood) object;
				currentTotalWoodHealth += wood.getHealth();
			}
		}
		return currentTotalWoodHealth / totalWoodHealth;
	}

	public double getFireExtinguishedPercent()
	{
		if (totalFireCount == 0)
		{
			return 0;
		}
		return ((double) extinguishedFireCount) / totalFireCount;
	}

	public double getAgentCountPercent()
	{
		if (totalAgentCount == 0)
		{
			return 0;
		}
		Context<Object> context = SimulationManager.getContext();
		double currentAgentCount = context.getObjects(ForesterAgent.class)
				.size();
		return currentAgentCount / totalAgentCount;
	}

	public void reset()
	{
		totalWoodHealth = 0;
		totalAgentCount = 0;
		totalFireCount = 0;
		extinguishedFireCount = 0;
		covering = new long[gridHeight][gridWidth];
	}
}
