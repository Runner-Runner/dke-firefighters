package agent;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import environment.Fire;
import environment.Wood;

public class Statistic 
{
	private double totalWoodHealth;
	private double totalAgentCount;
	private int totalFireCount = 0;
	private int extinguishedFireCount = 0;
	
	public static Statistic getStatisticFromContext(Context<Object> context)
	{
		IndexedIterable<Object> objects = context.getObjects(Statistic.class);
		if(objects.size() != 1)
		{
			return null;
		}
		Object object = objects.get(0);
		if(object instanceof Statistic)
		{
			return (Statistic)object;
		}
		return null;
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
		Context<Object> context = ContextUtils.getContext(this);
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
	
	public double getWoodHealthPercent()
	{
		if(totalWoodHealth == 0)
		{
			return 0;
		}
		Context<Object> context = ContextUtils.getContext(this);
		IndexedIterable<Object> objects = context.getObjects(Wood.class);
		double currentTotalWoodHealth = 0;
		for(Object object : objects)
		{
			if(object instanceof Wood)
			{
				Wood wood = (Wood)object;
				currentTotalWoodHealth += wood.getHealth();
			}
		}
		return currentTotalWoodHealth / totalWoodHealth;
	}
	
	public double getFireExtinguishedPercent()
	{
		if(totalFireCount == 0)
		{
			return 0;
		}
		return ((double)extinguishedFireCount) / totalFireCount;
	}
	
	public double getAgentCountPercent()
	{
		if(totalAgentCount == 0)
		{
			return 0;
		}
		Context<Object> context = ContextUtils.getContext(this);
		double currentAgentCount = context.getObjects(ForesterAgent.class).size();
		return currentAgentCount / totalAgentCount;
	}
}
