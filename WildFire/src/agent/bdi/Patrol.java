package agent.bdi;

import repast.simphony.space.grid.GridPoint;
import statistics.Statistic;
import agent.ForesterAgent;
import agent.ForesterAgent.AgentInformation;
import agent.communication.CommunicationTool;
import agent.communication.request.InformationRequest;

public class Patrol extends Action {
	private int timeSinceDistanceCheck = DISTANCE_CHECK_RATE;
	private static final int DISTANCE_CHECK_RATE = 5;
	private double sendingRange;
	Double lastxVector = null;
	Double lastyVector = null;
	
	public Patrol() {
		super(1, 0);
		sendingRange = -1;
	}

	@Override
	protected boolean isInExecutePosition(ForesterAgent agent, GridPoint gp) {
		//no positional condition
		return true;
	}
	
	@Override
	public boolean executeInner(ForesterAgent agent, GridPoint gp) {
		timeSinceDistanceCheck++;
		if(sendingRange == -1){
			int gridWidth = Statistic.getInstance().getGridWidth();
			int gridHeight = Statistic.getInstance().getGridHeight();
			sendingRange = (gridWidth + gridHeight)/(Statistic.getInstance().getTotalAgentCount());
			sendingRange = Math.min(sendingRange, 5);
		}
		if(timeSinceDistanceCheck >= DISTANCE_CHECK_RATE)
		{
			CommunicationTool communicationTool = agent.getCommunicationTool();
			communicationTool.setSendingRange((int)sendingRange);
			communicationTool.sendRequest(new InformationRequest(agent.getCommunicationId(), 1, null, AgentInformation.class));
			timeSinceDistanceCheck = 0;
		}
		
		AgentInformation closestAgentInformation = CommunicationTool.
				getClosestAgentInformation(agent, (int)sendingRange);
		
		GridPoint ownPosition = agent.getPosition();
		int xTarget;
		int yTarget;
		GridPoint center = Statistic.getInstance().getCenter();
		
		//if no agent nearby or agent is at the forest border, move towards the center
		final int BORDER_SIZE = 1;
		int gridHeight = Statistic.getInstance().getGridHeight();
		int gridWidth = Statistic.getInstance().getGridWidth();
		if(ownPosition.getX()-ForesterAgent.SEEING_RANGE <= BORDER_SIZE 
				|| ownPosition.getX()+ForesterAgent.SEEING_RANGE >= gridWidth - BORDER_SIZE
				|| ownPosition.getY()-ForesterAgent.SEEING_RANGE <= BORDER_SIZE
				|| ownPosition.getY()+ForesterAgent.SEEING_RANGE >= gridHeight - BORDER_SIZE)
		{
			xTarget = center.getX();
			yTarget = center.getY();
			lastxVector = null;
			lastyVector = null;
		}
		else if(closestAgentInformation == null)
		{
			if(lastxVector == null)
			{
				xTarget = center.getX();
				yTarget = center.getY();
			}
			else
			{
				xTarget = (int)(ownPosition.getX() + lastxVector);
				yTarget = (int)(ownPosition.getY() + lastyVector);
			}
		}
		else
		{
			double xDiff = ownPosition.getX() - closestAgentInformation.getPosition().getX(); 
			double yDiff = ownPosition.getY() - closestAgentInformation.getPosition().getY();
			
			double xVector = xDiff;// / norm * agent.getSpeed();
			double yVector = yDiff;// / norm * agent.getSpeed();
			
			xTarget = (int)(ownPosition.getX() + xVector);
			yTarget = (int)(ownPosition.getY() + yVector);
		
				
			lastxVector = xVector;
			lastyVector = yVector;
			
		}
		agent.moveTowards(new GridPoint(xTarget, yTarget));
		
		return true;
	}
}
