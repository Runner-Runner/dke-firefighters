package agent;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import environment.Cloud.CloudInformation;
import environment.Fire.FireInformation;
import environment.Wind.WindInformation;

public class CommunicationTool {

	private ForesterAgent sender;
	private Grid<Object> grid;
	
	/**
	 * Local multicast sending range in grid tiles (in each cardinal direction). Null means send to all other agents (global broadcast).
	 */
	private Integer sendingRange;
	
	//TODO Add additional attribute for sending messages to one specific recipient
	
	public CommunicationTool(ForesterAgent sender, Grid<Object> grid)
	{
		this.sender = sender;
		this.grid = grid;
	}
	
	public void setSendingRange(int sendingRange)
	{
		this.sendingRange = sendingRange;
	}
	
	public void sendWindInformation(WindInformation windInformation)
	{
		List<ForesterAgent> recipients = getRecipients();
		for(ForesterAgent recipient : recipients)
		{
			recipient.getKnowledge().setWindInformation(windInformation);
		}
	}
	
	public void sendCloudInformation(CloudInformation cloudInformation)
	{
		List<ForesterAgent> recipients = getRecipients();
		for(ForesterAgent recipient : recipients)
		{
			recipient.getKnowledge().getCloudInformationMap().addInformation(cloudInformation);
		}
	}
	
	public void sendFireInformation(FireInformation fireInformation)
	{
		List<ForesterAgent> recipients = getRecipients();
		for(ForesterAgent recipient : recipients)
		{
			recipient.getKnowledge().getFireInformationMap().addInformation(fireInformation);
		}
	}
	
	private List<ForesterAgent> getRecipients()
	{
		List<ForesterAgent> recipients = new ArrayList<>();
		
		GridPoint senderLocation = grid.getLocation(sender);
		
		Context<Object> context = ContextUtils.getContext(this);
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
