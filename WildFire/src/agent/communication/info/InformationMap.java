package agent.communication.info;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.space.grid.GridPoint;

public class InformationMap<T extends Information> {
	private Map<String, T> informationMap;
	
	public static final String DELIMITER = "#"; 
	
	public InformationMap() {
		informationMap = new HashMap<>();
	}
	
	public boolean addInformation(T information) {
		String key = information.getPosition().getX() + DELIMITER + information.getPosition().getY();
		T oldInformation = informationMap.get(key);
		if(information.isNewerInformation(oldInformation))
		{
			//overwrite old information
			informationMap.put(key, information);
			return true;
		}
		return false;
	}
	
	public boolean removeInformation(int x, int y)
	{
		String key = x + DELIMITER + y;
		return informationMap.remove(key) == null;
	}
	
	public T getInformation(GridPoint position)
	{
		
		String key = position.getX() + DELIMITER + position.getY();
		return informationMap.get(key);
	}
	
	public Collection<T> getAllInformation()
	{
		return informationMap.values();
	}
}
