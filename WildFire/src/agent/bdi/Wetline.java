package agent.bdi;

import main.SimulationManager;
import agent.ForesterAgent;
import repast.simphony.space.grid.GridPoint;

public class Wetline extends Action
{
	public Wetline()
	{
		super(2, 5);
	}

	@Override
	protected boolean isInExecutePosition(ForesterAgent agent, GridPoint gp)
	{
		return SimulationManager.inMooreRange(agent, gp);
	}

	@Override
	public boolean executeInner(ForesterAgent agent, GridPoint gp)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
