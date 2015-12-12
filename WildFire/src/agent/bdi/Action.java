package agent.bdi;

import agent.ForesterAgent;
import repast.simphony.space.grid.GridPoint;

/**
 * Represents a simple action an agent intents to carry out.
 */
public abstract class Action
{
	/**
	 * Bounty the agent receives every time step.
	 */
	protected double incrementalBounty; 
	
	/**
	 * Bounty the agent receives after completing the action.
	 */
	protected double finalBounty;

	protected Action(double incremental, double finalBounty)
	{
		this.incrementalBounty = incremental;
		this.finalBounty = finalBounty;
	}

	public double getIncrementalBounty()
	{
		return incrementalBounty;
	}

	public double getFinalBounty()
	{
		return finalBounty;
	}

	/**
	 * Depending on whether the action is executable from the agent's current position, executes it 
	 * or moves towards the action's position.
	 * 
	 * @param agent
	 * @param gp
	 * @return True if the action can still be carried out, false if the action is 
	 * considered not executable anymore (e.g. because it is completed).
	 */
	public boolean execute(ForesterAgent agent, GridPoint gp)
	{
		if (isInExecutePosition(agent, gp))
		{
			return executeInner(agent, gp);
		} 
		else
		{
			agent.moveTowards(gp);
			return true;
		}
	}

	/**
	 * Checks whether the action is executable from the agent's current position by comparing it 
	 * against the position where the action shall be executed (if applicable).
	 * 
	 * @param agent
	 * @param gp
	 * @return
	 */
	protected abstract boolean isInExecutePosition(ForesterAgent agent,
			GridPoint gp);

	/**
	 * Performs the actual execution of the action for the current time step.
	 * 
	 * @param agent
	 * @param gp
	 * @return True if the action can still be carried out, false if the action is 
	 * considered not executable anymore (e.g. because it is completed).
	 */
	protected abstract boolean executeInner(ForesterAgent agent, GridPoint gp);
}
