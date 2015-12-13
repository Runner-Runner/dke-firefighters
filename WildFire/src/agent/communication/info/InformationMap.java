package agent.communication.info;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.space.grid.GridPoint;

/**
 * Represents a storage for a certain type of information. New information can
 * be added or removed, older information at the same position is overwritten.
 * Makes up a part of the forester agent's belief state.
 *
 * @param <T> Subtype of entity information
 */
public class InformationMap<T extends Information>
{
	/**
	 * Map hashing information by their position values
	 */
	protected Map<String, T> informationMap;

	public static final String DELIMITER = "#";

	public InformationMap()
	{
		informationMap = new HashMap<>();
	}

	/**
	 * @param information
	 * @return Whether this information overwrote an older information.
	 */
	public boolean addInformation(T information)
	{
		String key = information.getPosition().getX() + DELIMITER
				+ information.getPosition().getY();
		T oldInformation = informationMap.get(key);
		if (information.isNewerInformation(oldInformation))
		{
			// overwrite old information
			informationMap.put(key, information);
			return true;
		}
		return false;
	}

	/**
	 * Delete the information at the given position.
	 * 
	 * @param x
	 * @param y
	 * @return Whether information was actually removed.
	 */
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

	/**
	 * @return All information units of this map.
	 */
	public Collection<T> getAllInformation()
	{
		return informationMap.values();
	}
}
