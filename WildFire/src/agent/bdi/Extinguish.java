package agent.bdi;

import main.SimulationManager;
import agent.ForesterAgent;
import repast.simphony.space.grid.GridPoint;

/**
 * Action "extinguish fire". Time-consuming: Takes up a time step to execute.
 */
public class Extinguish extends Action
{

	public Extinguish()
	{
		super(3, 20);
	}

	@Override
	protected boolean isInExecutePosition(ForesterAgent agent, GridPoint gp)
	{
		return SimulationManager.inMooreRange(agent, gp);
	}

	@Override
	public boolean executeInner(ForesterAgent agent, GridPoint gp)
	{
		if (agent.extinguishFire(gp))
		{
			//bounty dependent on the amount of fire the agent extinguished in this time step.
			agent.addBounty(incrementalBounty * agent.getExtinguishedFireAmount());
			return true;
		} else
		{
			agent.addBounty(finalBounty);
			return false;
		}
	}
}
