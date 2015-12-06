package agent.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import main.CommonKnowledge;
import agent.ForesterAgent;
import agent.ForesterAgent.AgentInformation;
import agent.communication.info.Information;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestDismiss;
import agent.communication.request.RequestOffer;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;

public class CommunicationTool 
{

	private ForesterAgent sender;
	private Grid<Object> grid;
	
	/**
	 * Local multicast sending range in grid tiles (in each cardinal direction). Null means send to all other agents (global broadcast).
	 */
	private Double sendingRange;
	
	public CommunicationTool(ForesterAgent sender, Grid<Object> grid)
	{
		this.sender = sender;
		this.grid = grid;
	}
	
	public void setSendingRange(double sendingRange)
	{
		this.sendingRange = sendingRange;
	}
	
	public double sendInformation(Information information)
	{
		return sendInformation(getAgentsInRange(sender, sendingRange), information);
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
		return sendRequest(request, getAgentsInRange(sender, sendingRange));
	}
	public double sendRequest(List<String> recipientIDs, Request request){
		return sendRequest(request, getAgents(recipientIDs));
	}
	public double sendRequest(Request request, String id){
		return sendRequest(request, getAgent(id));
	}
	public double sendRequestOffer(String id, RequestOffer ro){
		ForesterAgent recipient = getAgent(id);
		
		if(recipient == null)
		{
			//recipient not reachable, possibly dead
			return 0;
		}
		
		recipient.receiveOffer(ro);
		return calculateDistance(sender, recipient);
	}
	public double sendRequestDismiss(String id, RequestDismiss dismiss){
		ForesterAgent recipient = getAgent(id);
		
		if(recipient == null)
		{
			//recipient not reachable, possibly dead
			return 0;
		}
		
		recipient.receiveDismiss(dismiss);
		return calculateDistance(sender, recipient);
	}
	public double sendRequestConfirm(String id, RequestConfirm rc){
		ForesterAgent recipient = getAgent(id);
		
		if(recipient == null)
		{
			//recipient not reachable, possibly dead
			return 0;
		}
		
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
		if(recipient == null)
		{
			//recipient not reachable, possibly dead
			return 0;
		}
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
		if(recipient == null)
		{
			//recipient not reachable, possibly dead
			return 0;
		}
		
		recipient.receiveRequest(request);
		return calculateDistance(sender, recipient);
	}
	
	private List<ForesterAgent> getAgents(List<String> ids){
		ArrayList<ForesterAgent> list = new ArrayList<ForesterAgent>(ids.size());
		Context<Object> context = CommonKnowledge.getContext();
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
		Context<Object> context = CommonKnowledge.getContext();
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
	
	public double calculateDistance(ForesterAgent a, ForesterAgent b){
		GridPoint aP = grid.getLocation(a);
		GridPoint bP = grid.getLocation(b);
		return calculateDistance(aP, bP);
	}
	
	public static double calculateDistance(GridPoint start, GridPoint end){
		return Math.sqrt(Math.pow(start.getX()-end.getX(), 2)+Math.pow(start.getY()-end.getY(), 2));
	}
	
	public static boolean inMooreRange(ForesterAgent agent, GridPoint gp)
	{
		double distance = calculateDistance(agent.getPosition(), gp);
		return distance <= 1.5;
	}
	
	private List<ForesterAgent> getAgentsInRange(ForesterAgent originAgent, Double sendingRange)
	{
		List<ForesterAgent> recipients = new ArrayList<>();
		
		Context<Object> context = CommonKnowledge.getContext();
		IndexedIterable<Object> objects = context.getObjects(ForesterAgent.class);
		
		for(Object obj : objects)
		{
			if(obj instanceof ForesterAgent)
			{
				if(obj.equals(this.sender))
				{
					continue;
				}
				ForesterAgent agent = (ForesterAgent)obj;
				if(sendingRange == null || calculateDistance(
						originAgent.getPosition(), agent.getPosition())<=sendingRange)
				{
					recipients.add(agent);
				}
			}
		}
		return recipients;
	}
	
	public static List<AgentInformation> getAgentInformationInRange(ForesterAgent originAgent, Integer sendingRange)
	{
		List<AgentInformation> agentInformationList = new ArrayList<>();
		
		Collection<AgentInformation> allAgentInformation = originAgent.getBelief().
				getAllInformation(AgentInformation.class);
		for(AgentInformation agentInformation : allAgentInformation)
		{
			GridPoint gp = agentInformation.getPosition(); 
			if(sendingRange == null || 
					calculateDistance(originAgent.getPosition(), gp) <= sendingRange)
			{
				agentInformationList.add(agentInformation);
			}
		}
		return agentInformationList;
	}
	
	public static AgentInformation getClosestAgentInformation(ForesterAgent originAgent, Integer sendingRange)
	{
		AgentInformation closestAgentInformation = null;
		double smallestDistance = Double.MAX_VALUE;
		
		Collection<AgentInformation> allAgentInformation = originAgent.getBelief().
				getAllInformation(AgentInformation.class);
		for(AgentInformation agentInformation : allAgentInformation)
		{
			if(!agentInformation.isEmptyInstance()){
				GridPoint gp = agentInformation.getPosition(); 
				double distance = calculateDistance(originAgent.getPosition(), gp);
				if(distance < smallestDistance && distance != 0)
				{
					closestAgentInformation = agentInformation;
					smallestDistance = distance;
				}
			}
		}
		
		if(sendingRange == null || smallestDistance <= sendingRange)
		{
			return closestAgentInformation;
		}
		return null;
	}
	/**
	 * http://www.cse.chalmers.se/edu/year/2010/course/TDA361/grid.pdf
	 * @param start
	 * @param xDirection
	 * @param yDirection
	 * @return
	 */
	public static ArrayList<GridPoint> tilesInDirection(NdPoint start, NdPoint end){
		ArrayList<GridPoint> inDirection = new ArrayList<GridPoint>();
		double xDirection = end.getX()-start.getX();
		double yDirection = end.getY()-start.getY();
		int x = (int)start.getX();
		int y = (int) start.getY();
		int endX = (int)end.getX();
		int endY = (int)end.getY();
		int stepX;
		int stepY;
		double tMaxX=Double.MAX_VALUE;
		double tMaxY=Double.MAX_VALUE;
		double nextXVoxel;
		double nextYVoxel;
		if(xDirection>0){
			stepX = 1;
			nextXVoxel = x+1;
		}
		else{
			stepX=-1;
			nextXVoxel = x;
		}
		tMaxX = (nextXVoxel-start.getX())/xDirection;
		if(yDirection>0){
			stepY = 1;
			nextYVoxel = y+1;
		}
		else{
			stepY = -1;
			nextYVoxel = y;
		}
		tMaxY = (nextYVoxel-start.getY())/yDirection;
		double tDeltaX = 1/xDirection*stepX;
		double tDeltaY = 1/yDirection*stepY;
		
		GridPoint startGP = new GridPoint(x,y);
		inDirection.add(startGP);
		Grid<Object> grid = CommonKnowledge.getGrid();
		while(x!=endX || y!=endY){
			if(tMaxX<tMaxY){
				tMaxX+=tDeltaX;
				x+=stepX;
			}
			else if (tMaxY<tMaxX){
				tMaxY+=tDeltaY;
				y+=stepY;
			}
			else if (endX != x){
				tMaxX+=tDeltaX;
				x+=stepX;
			}
			else{
				tMaxY+=tDeltaY;
				y+=stepY;
			}
			if(x<0||y<0||x>grid.getDimensions().getHeight()||y>grid.getDimensions().getWidth())
				break;
			inDirection.add(new GridPoint(x,y));
		}
		return inDirection;
	}
	
	public static ArrayList<GridPoint> tilesInDirection(NdPoint start, GridPoint end){
		return tilesInDirection(start, new NdPoint(end.getX(),end.getY()));
	}
}
