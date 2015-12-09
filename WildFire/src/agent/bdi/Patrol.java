package agent.bdi;

import java.util.ArrayList;

import main.SimulationManager;
import environment.Wood.WoodInformation;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import statistics.Statistic;
import agent.ForesterAgent;
import agent.ForesterAgent.AgentInformation;
import agent.communication.CommunicationTool;
import agent.communication.request.InformationRequest;

public class Patrol extends Action
{
	private int timeSinceDistanceCheck = DISTANCE_CHECK_RATE;
	private static final int DISTANCE_CHECK_RATE = 5;
	private double sendingRange;
	Double lastxVector = null;
	Double lastyVector = null;

	public Patrol()
	{
		super(1, 0);
		sendingRange = -1;
	}
	public Patrol(double initialX, double initialY)
	{
		super(1, 0);
		sendingRange = -1;
		this.lastxVector = initialX;
		this.lastyVector = initialY;
	}

	@Override
	protected boolean isInExecutePosition(ForesterAgent agent, GridPoint gp)
	{
		// no positional condition
		return true;
	}

	@Override
	public boolean executeInner(ForesterAgent agent, GridPoint gp)
	{
		timeSinceDistanceCheck++;
		if (sendingRange == -1)
		{
			int gridWidth = Statistic.getInstance().getGridWidth();
			int gridHeight = Statistic.getInstance().getGridHeight();
			sendingRange = (gridWidth + gridHeight)
					/ (Statistic.getInstance().getTotalAgentCount());
			sendingRange = Math.min(sendingRange, 5);
		}
		if (timeSinceDistanceCheck >= DISTANCE_CHECK_RATE)
		{
			CommunicationTool communicationTool = agent.getCommunicationTool();
			communicationTool.setSendingRange((int) sendingRange);
			communicationTool.sendRequest(new InformationRequest(agent
					.getCommunicationId(), 1, null, AgentInformation.class));
			timeSinceDistanceCheck = 0;
		}

		AgentInformation closestAgentInformation = SimulationManager
				.getClosestAgentInformation(agent, (int) sendingRange);

		GridPoint ownPosition = agent.getPosition();
		NdPoint exactPosition = agent.getExactPosition();
		GridPoint center = Statistic.getInstance().getCenter();
		int xTarget = center.getX();
		int yTarget = center.getY();

		// if no agent nearby or agent is at the forest border, move towards the
		// center
		int gridHeight = Statistic.getInstance().getGridHeight();
		int gridWidth = Statistic.getInstance().getGridWidth();
		
		
		if (closestAgentInformation != null)
		{
			double newXVector = exactPosition.getX()
					- closestAgentInformation.getExactPosition().getX();
			double newYVector = exactPosition.getY()
					- closestAgentInformation.getExactPosition().getY();
			//exact opposite direction
			if(lastxVector != null && newXVector/lastxVector == newYVector/lastyVector)
			{
				double angle = Math.atan(newYVector/newXVector);
				angle+=Math.PI/13;
				lastxVector = 10.0;
				lastyVector = Math.tan(angle)*10;
			}
			else
			{
				lastxVector = newXVector;
				lastyVector = newYVector;
			}
		}
		else if (ownPosition.getX() - ForesterAgent.SEEING_RANGE <= 0
				|| ownPosition.getX() + ForesterAgent.SEEING_RANGE >= gridWidth
				|| ownPosition.getY() - ForesterAgent.SEEING_RANGE <= 0
				|| ownPosition.getY() + ForesterAgent.SEEING_RANGE >= gridHeight)
		{
			lastxVector = null;
			lastyVector = null;
		} 
		// check direction for wood
		if (lastxVector != null && lastyVector != null)
		{
			
			boolean woodInDirection = false;
			ArrayList<GridPoint> directionTiles = SimulationManager
					.tilesInDirection(exactPosition, 
							lastxVector, lastyVector,new GridPoint(Integer.MAX_VALUE, Integer.MAX_VALUE));
			for (GridPoint tile : directionTiles)
			{
				if(!tile.equals(ownPosition))
				{
					WoodInformation wi = agent.getBelief().getInformation(tile,
							WoodInformation.class);
					if (wi == null || !wi.isEmptyInstance())
					{
						woodInDirection = true;
						break;
					}
				}
			}
			if (!woodInDirection)
			{
				WoodInformation lastCheckedWood = null;
				for (WoodInformation wi : agent.getBelief().getAllInformation(
						WoodInformation.class))
				{
					if (!wi.isEmptyInstance()
							&& (lastCheckedWood == null || lastCheckedWood
									.getTimestamp() > wi.getTimestamp()))
					{
						lastCheckedWood = wi;
					}
				}
				lastxVector = lastCheckedWood.getPosition().getX()
						- exactPosition.getX();
				lastyVector = lastCheckedWood.getPosition().getY()
						- exactPosition.getY();
				xTarget = lastCheckedWood.getPosition().getX();
				yTarget = lastCheckedWood.getPosition().getY();
			}
			else
			{
				xTarget = (int)(exactPosition.getX()+lastxVector);
				yTarget = (int)(exactPosition.getY()+lastyVector);
			}
		}
		agent.moveTowards(new GridPoint(xTarget, yTarget));

		return true;
	}
}
