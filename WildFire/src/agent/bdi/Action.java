package agent.bdi;

import agent.ForesterAgent;
import repast.simphony.space.grid.GridPoint;

public abstract class Action {
	protected double incrementalBounty; //bounty you get during the action
	protected double finalBounty; //bounty you get for a finished action (woodcutting)
	
	protected Action(double incremental, double finalBounty){
		this.incrementalBounty = incremental;
		this.finalBounty = finalBounty;
	}
	
	public double getIncrementalBounty(){
		return incrementalBounty;
	}
	public double getFinalBounty(){
		return finalBounty;
	}

	public boolean execute(ForesterAgent agent, GridPoint gp)
	{
		if(isInExecutePosition(agent, gp))
		{
			return executeInner(agent, gp);
		}
		else
		{
			agent.moveTowards(gp);
			return true;
		}
	}
	
	protected abstract boolean isInExecutePosition(ForesterAgent agent, GridPoint gp);
	protected abstract boolean executeInner(ForesterAgent agent, GridPoint gp);
}
