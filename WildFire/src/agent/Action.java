package agent;

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
}
