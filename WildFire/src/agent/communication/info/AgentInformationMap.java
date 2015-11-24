package agent.communication.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import repast.simphony.engine.environment.RunEnvironment;
import agent.ForesterAgent.AgentInformation;

public class AgentInformationMap extends InformationMap<AgentInformation>{
	public boolean addInformation(AgentInformation information) {
		boolean addSuccess = super.addInformation(information);
		
		if(addSuccess)
		{
			String obsoleteKey = null;
			for(Entry<String, AgentInformation> entry : informationMap.entrySet())
			{
				AgentInformation agentInformation = entry.getValue();
				if(agentInformation.getCommunicationId().equals(information.getCommunicationId()) && 
						(!agentInformation.getPositionX().equals(information.getPositionX()) ||
						!agentInformation.getPositionY().equals(information.getPositionY())))
				{
					obsoleteKey = entry.getKey();
					break;
				}
			}
			if(obsoleteKey != null)
			{
				informationMap.remove(obsoleteKey);
			}
		}
		return addSuccess;
	}
	
	@Override
	public AgentInformation getInformation(int positionX, int positionY) {
		AgentInformation information = super.getInformation(positionX, positionY);
		double currentTimestep = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if(currentTimestep > information.getTimestamp()+3)
		{
			String key = positionX + DELIMITER + positionY;
			informationMap.remove(key);
			return null;
		}
		return super.getInformation(positionX, positionY);
	}
	
	@Override
	public Collection<AgentInformation> getAllInformation() {
		double currentTimestep = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		List<String> toBeRemovedKeys = new ArrayList<>();
		for(Entry<String, AgentInformation> entry : informationMap.entrySet())
		{
			if(currentTimestep > entry.getValue().getTimestamp()+3)
			{
				toBeRemovedKeys.add(entry.getKey());
			}
		}
		informationMap.keySet().removeAll(toBeRemovedKeys);
		return super.getAllInformation();
	}
}
