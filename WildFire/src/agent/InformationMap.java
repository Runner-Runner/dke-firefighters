package agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InformationMap<T extends Information> {
	private Map<String, T> informationMap;
	
	public static final String DELIMITER = "#"; 
	
	public InformationMap() {
		informationMap = new HashMap<>();
	}
	
	public boolean addInformation(T information) {
		String key = information.getPositionX() + DELIMITER + information.getPositionY();
		T oldInformation = informationMap.get(key);
		if(information.isNewerInformation(oldInformation))
		{
			//overwrite old information
			informationMap.put(key, information);
			return true;
		}
		return false;
	}
	
	public T getInformation(int positionX, int positionY)
	{
		String key = positionX + DELIMITER + positionY;
		return informationMap.get(key);
	}
	
	public Collection<T> getAllInformation()
	{
		return informationMap.values();
	}
}
