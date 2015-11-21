package agent.bdi;

import agent.ForesterAgent;
import repast.simphony.space.grid.GridPoint;

public class Extinguish extends Action{

	public Extinguish() {
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
