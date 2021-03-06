package agent.bdi;

import java.util.HashMap;

import repast.simphony.space.grid.GridPoint;

/**
 * Represents an agent's intention to execute an action at a certain position. 
 * If this action was suggested from an external source, also stores the requester 
 * who asked for this action to be carried out.
 */
public class Intention
{
	private Action action;
	private GridPoint position;
	private HashMap<Integer, String> requester;

	public Intention(Action action, GridPoint position, String requesterId,
			Integer requestId)
	{
		super();
		this.action = action;
		this.position = position;
		this.requester = new HashMap<>();
		addRequester(requesterId, requestId);
	}

	public HashMap<Integer, String> getRequester()
	{
		return requester;
	}

	public void addRequester(String requesterId, Integer requestId)
	{
		if (requesterId != null)
		{
			this.requester.put(requestId, requesterId);
		}
	}

	public boolean removeRequester(Integer requestId)
	{
		return this.requester.remove(requestId) == null;
	}

	public Action getAction()
	{
		return action;
	}

	public GridPoint getPosition()
	{
		return this.position;
	}

}
