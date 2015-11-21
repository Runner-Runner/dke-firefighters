package agent.communication;

import java.util.ArrayList;
import java.util.List;

import agent.ForesterAgent;
import agent.communication.info.Information;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestOffer;
import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

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
	
	public double sendInformation(Information information)
	{
		return sendInformation(getRangeRecipients(),information);
	}
	
	public double sendInformation(Information information, List<String> recipientIDs){
		List<ForesterAgent> list = getAgents(recipientIDs);
		return sendInformation(list, information);
	}
	
	public double sendInformation(Information information, String id){
		return sendInformation(information, getAgent(id));
	}
	public double sendRequest(Request request)
	{
		return sendRequest(request, getRangeRecipients());
	}
	public double sendRequest(List<String> recipientIDs, Request request){
		return sendRequest(request, getAgents(recipientIDs));
	}
	public double sendRequest(Request request, String id){
		return sendRequest(request, getAgent(id));
	}
	public double sendRequestOffer(String id, RequestOffer ro){
		ForesterAgent recipient = getAgent(id);
		recipient.receiveOffer(ro);
		return calculateDistance(sender, recipient);
	}
	public double sendRequestConfirm(String id, RequestConfirm rc){
		ForesterAgent recipient = getAgent(id);
		recipient.receiveConfirmation(rc);
		return calculateDistance(sender, recipient);
	}
	
	private double sendInformation(List<ForesterAgent> recipients, Information information)
	{
		double sum = 0;
		for(ForesterAgent recipient : recipients)
		{
			sum+=sendInformation(information, recipient);
		}
		return sum;
	}
	private double sendInformation(Information information, ForesterAgent recipient)
	{
		recipient.receiveInformation(information);
		return calculateDistance(sender, recipient);
	}
	private double sendRequest(Request request, List<ForesterAgent> recipients)
	{
		double sum = 0;
		for(ForesterAgent recipient : recipients)
		{
			sum+=sendRequest(request, recipient);
		}
		return sum;
	}
	private double sendRequest(Request request, ForesterAgent recipient)
	{
		recipient.receiveRequest(request);
		return calculateDistance(sender, recipient);
	}
	private List<ForesterAgent> getAgents(List<String> ids){
		ArrayList<ForesterAgent> list = new ArrayList<ForesterAgent>(ids.size());
		Context<Object> context = ContextUtils.getContext(sender);
		IndexedIterable<Object> objects = context.getObjects(ForesterAgent.class);
		for(Object obj : objects)
		{
			if(obj instanceof ForesterAgent)
			{
				ForesterAgent agent = (ForesterAgent)obj;
				if(ids.contains(agent.getCommunicationId()))
					list.add(agent);
			}
		}
		return list;
	}
	private ForesterAgent getAgent(String id){
		Context<Object> context = ContextUtils.getContext(sender);
		IndexedIterable<Object> objects = context.getObjects(ForesterAgent.class);
		
		for(Object obj : objects)
		{
			if(obj instanceof ForesterAgent)
			{
				ForesterAgent agent = (ForesterAgent)obj;
				if(agent.getCommunicationId().equals(id)){
					return agent;
				}
			}
		}
		return null;
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
				
				if(sendingRange == null || calculateDistance(sender, agent)<=sendingRange)
				{
					recipients.add(agent);
				}
			}
		}
		return recipients;
	}
	
	private double calculateDistance(ForesterAgent a, ForesterAgent b){
		GridPoint aP = grid.getLocation(a);
		GridPoint bP = grid.getLocation(b);
		return Math.sqrt(Math.pow(aP.getX()-bP.getX(), 2)+Math.pow(aP.getY()-bP.getY(), 2));
	}
}
