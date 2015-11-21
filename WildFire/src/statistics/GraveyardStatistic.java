package statistics;

/**
 * Stores data from agents that died and are removed from the context, thus preventing 
 * their contribution to the simulation from getting lost.
 */
public class GraveyardStatistic implements DataProviderExtinguishedFireAmount {
	private double sumExtinguishedFireAmount = 0;
	
	private static GraveyardStatistic graveyardStatistic;
	
	private GraveyardStatistic()
	{
		//Singleton
	}
	
	public static GraveyardStatistic getInstance()
	{
		if(graveyardStatistic == null)
		{
			graveyardStatistic = new GraveyardStatistic();
		}
		return graveyardStatistic;
	}
	
	public void addExtinguishedFireAmount(double extinguishedFireAmount)
	{
		sumExtinguishedFireAmount += extinguishedFireAmount;
	}
	
	@Override
	public double getExtinguishedFireAmount()
	{
		return sumExtinguishedFireAmount;
	}
}
