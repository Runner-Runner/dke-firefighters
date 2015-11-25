package main;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;


public class CommonKnowledge {
	public static final int GENERAL_SCHEDULE_TICK_RATE = 1;
	public static final int CLOUD_FACTOR = 10;
	public static final int FIRE_FACTOR = 5;
	public static final int WIND_FACTOR = 3;
	
	private static Context<Object> context;
	private static Grid<Object> grid;
	private static ContinuousSpace<Object> space;
	
	public static void setContext(Context<Object> c)
	{
		CommonKnowledge.context = c;
	}
	public static Context<Object> getContext()
	{
		return CommonKnowledge.context;
	}
	public static Grid<Object> getGrid()
	{
		return CommonKnowledge.grid;
	}
	public static ContinuousSpace<Object> getSpace()
	{
		return CommonKnowledge.space;
	}
	public static void setGrid(Grid<Object> g)
	{
		CommonKnowledge.grid = g;
	}
	public static void setSpace(ContinuousSpace<Object>s)
	{
		CommonKnowledge.space = s;
	}
	
}
