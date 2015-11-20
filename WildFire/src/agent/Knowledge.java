package agent;

import java.util.Collection;
import java.util.HashMap;

import agent.ForesterAgent.AgentInformation;
import environment.Cloud.CloudInformation;
import environment.Fire.FireInformation;
import environment.Wind.WindInformation;

public class Knowledge {
	private WindInformation windInformation;
	private InformationMap<CloudInformation> cloudInformationMap;
	private InformationMap<FireInformation> fireInformationMap;
	private InformationMap<AgentInformation> agentInformationMap;

	private HashMap<Class<? extends Information>, InformationMap<? extends Information>> specificInformationMap;
	
	public Knowledge() {
		specificInformationMap = new HashMap<>();
		
		cloudInformationMap = new InformationMap<>();
		fireInformationMap = new InformationMap<>();
		agentInformationMap = new InformationMap<>();
		
		specificInformationMap.put(CloudInformation.class, cloudInformationMap);
		specificInformationMap.put(FireInformation.class, fireInformationMap);
		specificInformationMap.put(AgentInformation.class, agentInformationMap);
	}
	
	public <T extends Information> Collection<T> getAllInformation(Class<T> informationClass)
	{
		InformationMap<? extends Information> informationMap = specificInformationMap.get(informationClass);
		@SuppressWarnings("unchecked")
		Collection<T> allInformation = (Collection<T>)informationMap.getAllInformation();
		return allInformation;
	}
	
	public <T extends Information> boolean addInformation(T information) 
	{
		@SuppressWarnings("unchecked")
		InformationMap<T> informationMap = (InformationMap<T>)specificInformationMap.get(information.getClass());
		return informationMap.addInformation(information);
	}
	
	public <T extends Information> boolean removeInformation(int positionX, int positionY, Class<T> informationClass) 
	{
		InformationMap<? extends Information> informationMap = specificInformationMap.get(informationClass);
		return informationMap.removeInformation(positionX, positionY);
	}
	
	public <T extends Information> T getInformation(int positionX, int positionY, Class<T> informationClass)
	{
		InformationMap<? extends Information> informationMap = specificInformationMap.get(informationClass);
		Information information = informationMap.getInformation(positionX, positionY);
		return informationClass.cast(information);
	}
	
	public void setWindInformation(WindInformation windInformation)
	{
		if(windInformation.isNewerInformation(this.windInformation))
		{
			this.windInformation = windInformation;
		}
	}

	public WindInformation getWindInformation()
	{
		return windInformation;
	}
}
