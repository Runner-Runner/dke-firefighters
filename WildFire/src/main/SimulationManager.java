package main;

import java.util.ArrayList;
import java.util.Collection;

import agent.ForesterAgent;
import agent.ForesterAgent.AgentInformation;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

/**
 * Provides generally known information and helper methods.
 * 
 * @author Daniel
 *
 */
public class SimulationManager
{
	public static final int GENERAL_SCHEDULE_TICK_RATE = 1;
	public static final int CLOUD_FACTOR = 10;
	public static final int FIRE_FACTOR = 5;
	public static final int WIND_FACTOR = 3;

	private static Context<Object> context;
	private static Grid<Object> grid;
	private static ContinuousSpace<Object> space;

	public static void setContext(Context<Object> c)
	{
		SimulationManager.context = c;
	}

	public static Context<Object> getContext()
	{
		return SimulationManager.context;
	}

	public static Grid<Object> getGrid()
	{
		return SimulationManager.grid;
	}

	public static ContinuousSpace<Object> getSpace()
	{
		return SimulationManager.space;
	}

	public static void setGrid(Grid<Object> g)
	{
		SimulationManager.grid = g;
	}

	public static void setSpace(ContinuousSpace<Object> s)
	{
		SimulationManager.space = s;
	}

	/**
	 * @param originAgent
	 * @param sendingRange
	 * @return The information of the agent that is closest to the given agent within a certain sending range.
	 */
	public static AgentInformation getClosestAgentInformation(
			ForesterAgent originAgent, Integer sendingRange)
	{
		AgentInformation closestAgentInformation = null;
		double smallestDistance = Double.MAX_VALUE;

		Collection<AgentInformation> allAgentInformation = originAgent
				.getBelief().getAllInformation(AgentInformation.class);
		for (AgentInformation agentInformation : allAgentInformation)
		{
			if (!agentInformation.isEmptyInstance())
			{
				GridPoint gp = agentInformation.getPosition();
				double distance = calculateDistance(originAgent.getPosition(),
						gp);
				if (distance < smallestDistance && distance != 0)
				{
					closestAgentInformation = agentInformation;
					smallestDistance = distance;
				}
			}
		}

		if (sendingRange == null || smallestDistance <= sendingRange)
		{
			return closestAgentInformation;
		}
		return null;
	}

	/**
	 * http://www.cse.chalmers.se/edu/year/2010/course/TDA361/grid.pdf
	 * 
	 * @param start
	 * @param xDirection
	 * @param yDirection
	 * @return
	 */
	public static ArrayList<GridPoint> tilesInDirection(NdPoint start, double xDirection, double yDirection,
			GridPoint end)
	{
		ArrayList<GridPoint> inDirection = new ArrayList<GridPoint>();
		int x = (int) start.getX();
		int y = (int) start.getY();
		int endX = end.getX();
		int endY = end.getY();
		int stepX;
		int stepY;
		double tMaxX = Double.MAX_VALUE;
		double tMaxY = Double.MAX_VALUE;
		double nextXVoxel;
		double nextYVoxel;
		if (xDirection >= 0)
		{
			stepX = 1;
			nextXVoxel = x + 1;
		} else
		{
			stepX = -1;
			nextXVoxel = x;
		}
		tMaxX = (nextXVoxel - start.getX()) / xDirection;
		if (yDirection >= 0)
		{
			stepY = 1;
			nextYVoxel = y + 1;
		} else
		{
			stepY = -1;
			nextYVoxel = y;
		}
		tMaxY = (nextYVoxel - start.getY()) / yDirection;
		double tDeltaX = 1 / xDirection * stepX;
		double tDeltaY = 1 / yDirection * stepY;

		GridPoint startGP = new GridPoint(x, y);
		inDirection.add(startGP);
		Grid<Object> grid = SimulationManager.getGrid();
		while (x != endX || y != endY)
		{
			if (tMaxX < tMaxY)
			{
				tMaxX += tDeltaX;
				x += stepX;
			} else if (tMaxY < tMaxX)
			{
				tMaxY += tDeltaY;
				y += stepY;
			} else if (endX != x)
			{
				tMaxX += tDeltaX;
				x += stepX;
			} else
			{
				tMaxY += tDeltaY;
				y += stepY;
			}
			if (x < 0 || y < 0 || x >= grid.getDimensions().getHeight()
					|| y >= grid.getDimensions().getWidth())
				break;
			inDirection.add(new GridPoint(x, y));
		}
		return inDirection;
	}

	public static double calculateDistance(GridPoint start, GridPoint end)
	{
		return Math.sqrt(Math.pow(start.getX() - end.getX(), 2)
				+ Math.pow(start.getY() - end.getY(), 2));
	}

	public static boolean inMooreRange(ForesterAgent agent, GridPoint gp)
	{
		double distance = calculateDistance(agent.getPosition(), gp);
		return distance <= 1.5;
	}
}
