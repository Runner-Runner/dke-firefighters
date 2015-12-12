package agent.communication.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.grid.GridPoint;
import agent.ForesterAgent.AgentInformation;

/**
 * Special variation of the InformationMap. Stores information about agent's
 * positions as volatile information, meaning information older than a certain
 * threshold gets removed automatically (because agent's positions change almost
 * every time step). Also, if there exist multiple information units about the
 * same agent, the older ones get removed.
 */
public class AgentInformationMap extends InformationMap<AgentInformation>
{
	private static final int OBSOLETE_THRESHOLD = 3;

	public boolean addInformation(AgentInformation information)
	{
		boolean addSuccess = super.addInformation(information);

		if (addSuccess)
		{
			String obsoleteKey = null;
			for (Entry<String, AgentInformation> entry : informationMap
					.entrySet())
			{
				// check if agent is contained multiple times in the belief.
				// If so, remove oldest information
				AgentInformation agentInformation = entry.getValue();
				if (agentInformation.getCommunicationId().equals(
						information.getCommunicationId())
						&& !agentInformation.getPosition().equals(
								information.getPosition()))
				{
					obsoleteKey = entry.getKey();
					break;
				}
			}
			if (obsoleteKey != null)
			{
				informationMap.remove(obsoleteKey);
			}
		}
		return addSuccess;
	}

	@Override
	public AgentInformation getInformation(GridPoint position)
	{
		AgentInformation information = super.getInformation(position);
		if (information == null)
		{
			return null;
		}
		double currentTimestep = RunEnvironment.getInstance()
				.getCurrentSchedule().getTickCount();
		if (currentTimestep > information.getTimestamp() + OBSOLETE_THRESHOLD)
		{
			String key = position.getX() + DELIMITER + position.getY();
			informationMap.remove(key);
			return null;
		}
		return super.getInformation(position);
	}

	@Override
	public Collection<AgentInformation> getAllInformation()
	{
		double currentTimestep = RunEnvironment.getInstance()
				.getCurrentSchedule().getTickCount();
		List<String> toBeRemovedKeys = new ArrayList<>();
		for (Entry<String, AgentInformation> entry : informationMap.entrySet())
		{
			if (currentTimestep > entry.getValue().getTimestamp() + OBSOLETE_THRESHOLD)
			{
				toBeRemovedKeys.add(entry.getKey());
			}
		}
		informationMap.keySet().removeAll(toBeRemovedKeys);
		return super.getAllInformation();
	}
}
