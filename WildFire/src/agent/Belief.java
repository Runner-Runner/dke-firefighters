package agent;

import java.util.Collection;
import java.util.HashMap;

import agent.ForesterAgent.AgentInformation;
import agent.communication.info.AgentInformationMap;
import agent.communication.info.Information;
import agent.communication.info.InformationMap;
import environment.Cloud.CloudInformation;
import environment.Fire.FireInformation;
import environment.Wind.WindInformation;
import environment.Wood.WoodInformation;

public class Belief {
	private WindInformation windInformation;

	private HashMap<Class<? extends Information>, InformationMap<? extends Information>> specificInformationMap;
	
	public Belief() {
		specificInformationMap = new HashMap<>();
		
		specificInformationMap.put(CloudInformation.class, new InformationMap<CloudInformation>());
		specificInformationMap.put(FireInformation.class, new InformationMap<FireInformation>());
		specificInformationMap.put(WoodInformation.class, new InformationMap<WoodInformation>());
		specificInformationMap.put(AgentInformation.class, new AgentInformationMap());
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
		if(informationMap == null)
		{
			return false;
		}
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
