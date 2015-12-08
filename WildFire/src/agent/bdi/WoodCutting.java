package agent.bdi;

import repast.simphony.space.grid.GridPoint;
import agent.ForesterAgent;

public class WoodCutting extends Action
{
	public WoodCutting()
	{
		super(0, 20);
	}

	@Override
	protected boolean isInExecutePosition(ForesterAgent agent, GridPoint gp)
	{
		return agent.getPosition().equals(gp);
	}

	@Override
	public boolean executeInner(ForesterAgent agent, GridPoint gp)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
