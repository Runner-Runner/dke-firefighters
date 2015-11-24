package agent.bdi;

import repast.simphony.space.grid.GridPoint;
import statistics.Statistic;
import agent.ForesterAgent;
import agent.ForesterAgent.AgentInformation;
import agent.communication.CommunicationTool;
import agent.communication.request.InformationRequest;
import environment.Wood.WoodInformation;

public class Patrol extends Action {
	private int timeSinceDistanceCheck = DISTANCE_CHECK_RATE;
	private static final int DISTANCE_CHECK_RATE = 5;
	private double sendingRange;
	Double xVector = null;
	Double yVector = null;
	
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
			sendingRange = (gridWidth + gridHeight)/(2*Statistic.getInstance().getTotalAgentCount());
		}
		if(timeSinceDistanceCheck >= DISTANCE_CHECK_RATE)
		{
			CommunicationTool communicationTool = agent.getCommunicationTool();
			communicationTool.setSendingRange((int)sendingRange);
			communicationTool.sendRequest(new InformationRequest(agent.getCommunicationId(), 1, 
					null, null, AgentInformation.class));
			timeSinceDistanceCheck = 0;
		}
		
		AgentInformation closestAgentInformation = CommunicationTool.
				getClosestAgentInformation(agent, (int)sendingRange);
		
		GridPoint ownPosition = agent.getPosition();
		int xTarget = -1;
		int yTarget = -1;
		
		if(closestAgentInformation == null)
		{
			//TODO What to do?
			if(xVector == null)
			{
				xVector = 1.0;
				yVector = 0.0;
			}
			xTarget = (int)(ownPosition.getX() + xVector);
			yTarget = (int)(ownPosition.getY() + yVector);
		}
		else
		{
			double xDiff = ownPosition.getX() - closestAgentInformation.getPositionX(); 
			double yDiff = ownPosition.getY() - closestAgentInformation.getPositionY();
			double norm = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
			
			double xVector = xDiff / norm * agent.getSpeed();
			double yVector = yDiff / norm * agent.getSpeed();
			
			double[] xVectorValues = new double[]{xVector, -yVector, yVector, -xVector};
			double[] yVectorValues = new double[]{yVector, xVector, -xVector, -yVector};
			
			for(int i=0; i<4; i++)
			{
				xTarget = (int)(ownPosition.getX() + xVectorValues[i]);
				yTarget = (int)(ownPosition.getY() + yVectorValues[i]);
			
				if(xTarget<0 || yTarget<0 || xTarget >=Statistic.getInstance().getGridWidth() || 
						yTarget >= Statistic.getInstance().getGridHeight()){
					continue;
				}
				
				WoodInformation information = agent.getBelief().getInformation((int)xTarget, (int)yTarget, WoodInformation.class);
				if(information != null && !information.isEmptyInstance())
				{
					break;
				}
				xTarget = (int)(ownPosition.getX() + xVectorValues[0]);
				yTarget = (int)(ownPosition.getY() + yVectorValues[0]);
			}
			System.out.println("+++ "+xTarget + " " + yTarget + " " + ownPosition.getX() + " " + ownPosition.getY() + " " + closestAgentInformation.getPositionX() + " " + closestAgentInformation.getPositionY());
		}
		
		if(xTarget != -1)
		{
			agent.moveTowards(new GridPoint(xTarget, yTarget));
		}
		
		return true;
	}
}
