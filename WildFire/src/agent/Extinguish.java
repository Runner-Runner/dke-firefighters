package agent;

import repast.simphony.space.grid.GridPoint;

public class Extinguish extends Action{

	protected Extinguish() {
		super(3, 20);
	}

	@Override
	public boolean execute(ForesterAgent agent, GridPoint gp) {
		if(agent.extinguishFire(gp)){
			//TODO increase agents bounty
			return true;
		}
		return false;
	}
	
}
