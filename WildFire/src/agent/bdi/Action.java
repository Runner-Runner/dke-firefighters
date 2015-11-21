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
	public abstract boolean execute(ForesterAgent agent, GridPoint gp);
}
