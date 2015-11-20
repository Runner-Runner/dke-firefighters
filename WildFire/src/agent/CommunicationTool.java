package agent;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import environment.Wind.WindInformation;

public class CommunicationTool {

	private ForesterAgent sender;
	private Grid<Object> grid;
	
	/**
	 * Local multicast sending range in grid tiles (in each cardinal direction). Null means send to all other agents (global broadcast).
	 */
	private Integer sendingRange;
	
	public CommunicationTool(ForesterAgent sender, Grid<Object> grid)
	{
		this.sender = sender;
		this.grid = grid;
	}
	
	public void setSendingRange(int sendingRange)
	{
		this.sendingRange = sendingRange;
	}
	
	public void sendInformation(Information information)
	{
		sendInformation(information, getRangeRecipients());
	}
	
	public void sendInformation(Information information, List<ForesterAgent> recipients)
	{
		for(ForesterAgent recipient : recipients)
		{
			sendInformation(information, recipient);
		}
	}
	
	public void sendInformation(Information information, ForesterAgent recipient)
	{
		//special cases
		if(information instanceof WindInformation)
		{
			recipient.getBelief().setWindInformation((WindInformation) information);
		}
		else
		{
			recipient.getBelief().addInformation(information);
		}
	}
	
	private List<ForesterAgent> getRangeRecipients()
	{
		List<ForesterAgent> recipients = new ArrayList<>();
		
		GridPoint senderLocation = grid.getLocation(sender);
		
		Context<Object> context = ContextUtils.getContext(sender);
		IndexedIterable<Object> objects = context.getObjects(ForesterAgent.class);
		
		for(Object obj : objects)
		{
			if(obj instanceof ForesterAgent)
			{
				ForesterAgent agent = (ForesterAgent)obj;
				GridPoint location = grid.getLocation(agent);
				
				if(sendingRange == null ||
					(location.getX() >= senderLocation.getX() - sendingRange && 
					location.getX() <= senderLocation.getX() + sendingRange &&
					location.getY() >= senderLocation.getY() - sendingRange &&
					location.getY() <= senderLocation.getY() + sendingRange))
				{
					recipients.add(agent);
				}
			}
		}
		return recipients;
	}
}
