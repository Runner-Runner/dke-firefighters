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

/**
 * Action "patrol through forest". Time-consuming: Takes up a time step to execute (moving). Patroling has the 
 * goal to gain information about the forest and find newly sparked fires. While patroling, the agent takes the
 * positions of other agents into account and moves in a way to provide the best coverage of the forest.
 */
public class Patrol extends Action
{
	private int timeSinceDistanceCheck = DISTANCE_CHECK_RATE;

	/**
	 * Stores the last used sending range to be able to increase it if a sent message did not result in sufficient
	 * answers from other forester agents.
	 */
	private double sendingRange;
	
	/**
	 * Last x-direction the agent moved towards.
	 */
	private Double lastxVector = null;
	
	/**
	 * Last y-direction the agent moved towards.
	 */
	private Double lastyVector = null;

	/**
	 * Defines the time step rate in which the agent sends a request to gain information about other 
	 * surrounding agents.
	 */
	private static final int DISTANCE_CHECK_RATE = 5;

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
			//initial sending range dependent on grid size and initial number of agents in the grid
			int gridWidth = Statistic.getInstance().getGridWidth();
			int gridHeight = Statistic.getInstance().getGridHeight();
			sendingRange = (gridWidth + gridHeight)
					/ (Statistic.getInstance().getTotalAgentCount());
			sendingRange = Math.min(sendingRange, 4);
			sendingRange = 8;
		}
		
		if (timeSinceDistanceCheck >= DISTANCE_CHECK_RATE)
		{
			//request information about other surrounding agents.
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

		// if no agent nearby (in sending range) or agent is at the forest border, 
		// move towards the center
		int gridHeight = Statistic.getInstance().getGridHeight();
		int gridWidth = Statistic.getInstance().getGridWidth();
		
		
		if (closestAgentInformation != null)
		{
			double newXVector = exactPosition.getX()
					- closestAgentInformation.getExactPosition().getX();
			double newYVector = exactPosition.getY()
					- closestAgentInformation.getExactPosition().getY();
			//move in the opposite direction of the closest agent
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
		//boundary check
		else if (ownPosition.getX() - ForesterAgent.SEEING_RANGE+1 <= 0
				|| ownPosition.getX() + ForesterAgent.SEEING_RANGE >= gridWidth
				|| ownPosition.getY() - ForesterAgent.SEEING_RANGE+1 <= 0
				|| ownPosition.getY() + ForesterAgent.SEEING_RANGE >= gridHeight)
		{
			lastxVector = null;
			lastyVector = null;
		} 
		// check direction for wood - do not simply patrol on burned down forest tiles
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
				//move to the wood tile that was refreshed in the agent's belief the longest time ago.
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
