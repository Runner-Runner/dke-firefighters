package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import main.CommonKnowledge;
import agent.bdi.Intention;
import agent.bdi.Patrol;
import agent.communication.CommunicationTool;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import agent.communication.request.ActionRequest;
import agent.communication.request.InformationRequest;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestDismiss;
import agent.communication.request.RequestOffer;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;
import statistics.DataProviderExtinguishedFireAmount;
import statistics.GraveyardStatistic;
import environment.Cloud;
import environment.Cloud.CloudInformation;
import environment.Fire;
import environment.Fire.FireInformation;
import environment.Wind;
import environment.Wood;
import environment.Wood.WoodInformation;

public abstract class ForesterAgent implements InformationProvider, DataProviderExtinguishedFireAmount 
{
	private static int agentCount = 0;
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;

	// in distance per step
	protected double speed;
	// rate at which the fire heat is lowered when extinguishing
	protected double extinguishRate;
	// defines the number of time steps this forester experienced burning
	// injuries
	protected int health = STARTING_HEALTH;
	// how many time steps until the last burning injury
	private int regenerateTime = 0;
	// how much fire this agent extinguished so far
	private double extinguishedFireAmount = 0;
	//belief of environment (fire/wood/agents/wind/clouds)
	protected Belief belief;
	//tool to send information and requests to other agents
	protected CommunicationTool communicationTool;
	//action and position, the agent wants to execute next
	protected Intention currentIntention;
	//list of information other agents sent via communicationtool in the last iteration "mailbox for information"
	//trusting agents should integrate them in beliefs
	protected List<Information> messages;
	//list of requests other agents sent via communicationtool in the last iteration "mailbox for requests"
	protected List<InformationRequest> infoRequests;
	//ActionRequests: agent can decide to help/answer
	protected HashMap<Integer, ActionRequest> actionRequests;
	//other agents responds to your request
	protected List<RequestOffer> offers;
	//confirmation accepting your offer
	protected RequestConfirm requestConfirmation;
	//already confirmed agents, who changed their intention
	protected List<RequestDismiss> rejections;
	//bounty the agent gets for extinguish fire, wetline or wood-cutting
	protected double bounty;
	//costs the agent pays for communication
	protected double costs;
	
	private int lastDirection = 1;
	
	/**
	 * Does not have to be set. If set, represents a way for other agents 
	 * to directly communicate with this agent instance.
	 */
	protected String communicationId;
	
	//number of burning injuries it takes to kill a forester.
	protected final static int STARTING_HEALTH = 5;
	//defines the number of time steps it takes to regenerate 1 health point
	//(if injured).
	protected final static int REGENERATE_RATE = 15;
	//
	public final static int SEEING_RANGE = 2;

	public ForesterAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
		this.extinguishRate = extinguishRate;

		this.belief = new Belief();
		this.messages = new LinkedList<Information>();
		this.infoRequests = new LinkedList<InformationRequest>();
		this.actionRequests = new HashMap<Integer, ActionRequest>();
		this.offers = new LinkedList<RequestOffer>();
		this.rejections = new ArrayList<>();
		
		this.communicationTool = new CommunicationTool(this, grid);
		
		this.communicationId = this.getClass().toString()+"|"+(agentCount++);
		
		currentIntention = new Intention(new Patrol(), null, null, null);
	}
	
	/**
	 * used by other agents to communicate information
	 * agent will work with them in the next iteration-step
	 * @param information
	 */
	public void receiveInformation(Information information)
	{
		this.messages.add(information);
	}
	/**
	 * used by other agents to ask for information or help
	 * agent will handle them in the next iteration-step
	 * @param request
	 */
	public void receiveRequest(Request request)
	{
		if(request instanceof InformationRequest)
		{
			infoRequests.add((InformationRequest)request);
		}
		else if(request instanceof ActionRequest)
		{
			ActionRequest actionRequest = (ActionRequest)request;
			actionRequests.put(actionRequest.getId(), actionRequest);
		}
	}
	public void addCosts(double costs){
		this.costs+=costs;
	}
	public void addBounty(double bounty){
		this.bounty+=bounty;
	}
	
	public double getBounty() {
		return bounty;
	}
	public double getCosts() {
		return costs;
	}
	public void receiveConfirmation(RequestConfirm requestConfirmation)
	{
		this.requestConfirmation = requestConfirmation;
	}
	
	public void receiveOffer(RequestOffer ro){
		this.offers.add(ro);
	}
	
	public void receiveDismiss(RequestDismiss rd){
		this.rejections.add(rd);
	}
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 45)
	public void changeConditions(){
		// check if in burning environment
		if (isOnBurningTile()!=null) 
		{
			boolean lethal = burn();
			if (lethal) {
				return;
			}
		}
		regenerateTime++;
		regenerate();
	}
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 40)
	public abstract void checkNeighbourhood();
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 38)
	public abstract void doRequests();
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 37)
	public abstract void sendAnswers();
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 30)
	public abstract void checkResponses();
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE, priority = 20)
	public abstract void doActions();

	/**
	 * Extinguish fire in one grid space. Time-consuming action: takes up one
	 * time step
	 * 
	 * @param pt
	 */
	public boolean extinguishFire(GridPoint pt) 
	{
		GridPoint position = grid.getLocation(this);

		// check if fire position really is in the Moore neighborhood
		if (Math.abs(position.getX() - pt.getX()) > 1
				|| Math.abs(position.getY() - pt.getY()) > 1) 
		{
			// illegal action
			return false;
		}

		Iterable<Object> objects = grid.getObjectsAt(pt.getX(), pt.getY());
		Fire fire = null;
		for (Object obj : objects) 
		{
			if (obj instanceof Fire) 
			{
				fire = (Fire) obj;
				break;
			}
		}
		if (fire == null) {
			// no fire to extinguish
			FireInformation extinguished = new FireInformation(pt);
			belief.addInformation(extinguished);
			return false;
		}
		else{
			extinguishedFireAmount += fire.decreaseHeat(extinguishRate, true);
			return true;
		}
	}

	/**
	 * Obtain information about the weather conditions in the Moore neighborhood
	 */
	protected void checkWeather() {
		// TODO send newly obtained information to (nearby?) agents

		// get information about wind
		Context<Object> context = CommonKnowledge.getContext();
		IndexedIterable<Object> windObjects = context.getObjects(Wind.class);

		Wind wind = (Wind)windObjects.get(0);
		belief.setWindInformation(wind.getInformation());
		
		//get information about clouds
		GridPoint location = grid.getLocation(this);
		int startX = location.getX() - 1;
		int startY = location.getY() - 1;
		for (int xOffset = 0; xOffset < 3; xOffset++) {
			for (int yOffset = 0; yOffset < 3; yOffset++) {
				Iterable<Object> gridObjects = grid.getObjectsAt(startX
						+ xOffset, startY + yOffset);
				boolean foundCloud = false;
				for(Object obj : gridObjects)
				{
					if(obj instanceof Cloud)
					{
						Cloud cloud = (Cloud)obj;
						belief.addInformation(cloud.getInformation());
						foundCloud = true;
						break;
					}
				}

				if(!foundCloud)
				{
					belief.addInformation(new CloudInformation(new GridPoint(startX + xOffset, startY + yOffset)));
				}
			}
		}
	}

	/**
	 * moves agent to a further away GridPoint in an angle to it, whereby max distance is its speed
	 * (uses the center of the grids (e.g. 2.5| 3.5))
	 * @param pt target gridpoint
	 * @return if the agent actually moved
	 */
	public void moveTowards(GridPoint pt) {
		
		boolean moveSuccess = moveTowardsIfUnoccupied(pt);
		
		if(!moveSuccess)
		{
			//Tiles on direct route are occupied - make turn to detour
			NdPoint currentPos = space.getLocation(this);
			//Try gradually increasing turn up to (excl.) 360 degrees
			int[] directions = new int[]
			{
				1, -1	
			};
			if (lastDirection == -1)
			{
				directions[0] = -1;
				directions[1] = 1;
			}
			outer:
			for(int direction : directions)
			{
				for(int i=1; i<=12; i++)
				{
					double xDetourDiff = pt.getX() - currentPos.getX();
					double yDetourDiff = pt.getY() - currentPos.getY();
					
					double detourAngle = i * (1d/12) * Math.PI * direction;
					
					double cos = Math.cos(detourAngle);
					double sin = Math.sin(detourAngle);
					
					double xDetourVector = xDetourDiff * cos - yDetourDiff * sin;
					double yDetourVector = xDetourDiff * sin + yDetourDiff * cos;
					
					GridPoint gridDetourTarget = new GridPoint(
							(int)Math.round(currentPos.getX() + xDetourVector), 
							(int)Math.round(currentPos.getY() + yDetourVector));
					
					moveSuccess = moveTowardsIfUnoccupied(gridDetourTarget);
					if(moveSuccess)
					{
						//if turn > 180 degrees, remember direction to prevent loop 
						lastDirection = direction;
						
						break outer;
					}
				}
			}
		}
	}
	
	private boolean moveTowardsIfUnoccupied(GridPoint pt) 
	{
		NdPoint oldPos = space.getLocation(this);
		GridPoint oldGridPos = grid.getLocation(this);
		
		NdPoint target;
		double xDiff = pt.getX()-oldPos.getX();
		double yDiff = pt.getY()-oldPos.getY();
		double distance = Math.sqrt(Math.pow(xDiff,2)+Math.pow(yDiff,2));
		if(distance>speed){
			target = new NdPoint(oldPos.getX()+(speed/distance)*xDiff, oldPos.getY()+(speed/distance)*yDiff);
		}
		else{
			target = new NdPoint(pt.getX(),pt.getY());
		}
		
		if (target.getX() < 0 || target.getY() < 0 || 
				target.getX() >= grid.getDimensions().getWidth() || 
				target.getY() >= grid.getDimensions().getHeight()) 
		{
			//Out of the grid
			return false;
		}
		
		List<GridPoint> tiles = CommunicationTool.tilesInDirection(oldPos, target);
		GridPoint next = null;
		
		for(GridPoint gp:tiles)
		{
			if(!gp.equals(oldGridPos) && tileOccupied(gp))
			{
				break;
			}
			next = gp;
		}
		//If only the own tile is not occupied (and the target is another tile), 
		//do not consider this as a move
		if(next.equals(oldGridPos) && tiles.size() > 1)
		{
			next = null;
		}
		if(next != null){
			if(next.getX()==(int)target.getX() && next.getY()==(int)target.getY())
			{
				space.moveTo(this, target.getX(), target.getY());
			}
			else
			{
				space.moveTo(this, next.getX()+0.5, next.getY()+0.5);
			}
			grid.moveTo(this, next.getX(), next.getY());
			
			return true;
		}
		return false;
	}
	
	private boolean tileOccupied(GridPoint gp){
		for (Object o : grid.getObjectsAt(gp.getX(), gp.getY())) 
		{
			if (o instanceof Fire) 
			{
				return true;
			}
			else if(o instanceof ForesterAgent && !o.equals(this))
			{
				return true;
			}
		}
		return false;
	}

	



	public Grid<Object> getGrid()
	{
		return grid;
	}
	
	public NdPoint getExactPosition()
	{
		return space.getLocation(this);
	}
	
	public Fire isOnBurningTile() 
	{
		GridPoint location = grid.getLocation(this);
		Iterable<Object> objects = grid.getObjectsAt(location.getX(),
				location.getY());
		for (Object obj : objects) 
		{
			if (obj instanceof Fire) 
			{
				return (Fire)obj;
			}
		}
		return null;
	}

	/**

	 * Updates belief about fire in Moore neighborhood. This action does not require a time step.
	 * 
	 * @return All information that actually changed the belief
	 */
	protected List<Information> updateNeighborhoodBelief() 
	{
		List<Information> informationList = new ArrayList<>();

		GridCellNgh<Object> nghCreator = new GridCellNgh<>(grid, getPosition(), Object.class, 
				SEEING_RANGE, SEEING_RANGE);
		List<GridCell<Object>> neighborhood = nghCreator.getNeighborhood(true);
		Iterator<GridCell<Object>> nghIterator = neighborhood.iterator();
		while(nghIterator.hasNext())
		{
			GridCell<Object> cell = nghIterator.next();
			boolean foundFire = false;
			boolean foundWood = false;
			Iterable<Object> items = cell.items();
			for(Object obj : items)
			{
				if(obj instanceof Fire || obj instanceof ForesterAgent || obj instanceof Wood)
				{
					//ask other agent about fire and wood beliefs
					if(obj instanceof ForesterAgent)
					{
						ForesterAgent otherAgent = (ForesterAgent)obj;
						if(obj.equals(this)){
							continue;
						}
						for(FireInformation fi:otherAgent.getBelief().getAllInformation(FireInformation.class))
						{
							belief.addInformation(fi);
						}
						for(WoodInformation wi:otherAgent.getBelief().getAllInformation(WoodInformation.class))
						{
							belief.addInformation(wi);
						}
					}
					else if(obj instanceof Fire)
					{
						foundFire = true;
					}
					else if(obj instanceof Wood)
					{
						foundWood = true;
					}
					Information information = ((InformationProvider)obj).getInformation();
					boolean changed = belief.addInformation(information);
					if(changed)
					{
						informationList.add(information);
					}
					
				}				
			}
			
			if (!foundFire) 
			{
				FireInformation removeInformation = new FireInformation(cell.getPoint());
				boolean changed = belief.addInformation(removeInformation);
				if(changed)
				{
					informationList.add(removeInformation);
				}
			}
			if (!foundWood) 
			{
				WoodInformation removeInformation = new WoodInformation(cell.getPoint());
				boolean changed = belief.addInformation(removeInformation);
				if(changed)
				{
					informationList.add(removeInformation);
				}
			}
		}
		
		
		return informationList;
	}

	/**
	 * Returns true if the agent died from burning injuries.
	 * 
	 * @return
	 */
	protected boolean burn() {
		regenerateTime = 0;
		health--;

		if(health <= 0)
		{
			die();
			return true;
		}
		return false;
	}

	private void die()
	{
		GraveyardStatistic graveyardStatistic = GraveyardStatistic.getInstance();
		graveyardStatistic.addExtinguishedFireAmount(getExtinguishedFireAmount());
		
		Context<Object> context = CommonKnowledge.getContext();
		context.remove(this);
	}
	
	protected void regenerate()
	{
		if(regenerateTime % REGENERATE_RATE == 0)
		{
			if(health < STARTING_HEALTH)
			{
				health++;
			}
		}
	}

	
	public CommunicationTool getCommunicationTool()
	{
		return communicationTool;
	}
	
	@Override
	public double getExtinguishedFireAmount()
	{
		return extinguishedFireAmount;
	}
	
	public double getSpeed()
	{
		return speed;
	}
	
	public String getCommunicationId()
	{
		return communicationId;
	}
	
	public void setCommunicationId(String communicationId) 
	{
		this.communicationId = communicationId;
	}
	
	public GridPoint getPosition()
	{
		return grid.getLocation(this);
	}
	
	public Belief getBelief()
	{
		return belief;
	}
	
	@Override
	public AgentInformation getInformation() {
		GridPoint location = grid.getLocation(this);
		return new AgentInformation(communicationId, location, speed, health);
	}

	public static class AgentInformation extends Information {

		private String communicationId;
		private double speed;
		private int health;

		private AgentInformation(String communicationId, GridPoint position, double speed, int health) {
			super(position);
			this.communicationId = communicationId;
			this.speed = speed;
			this.health = health;
		}

		//TODO Delete remove const?
		
		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public AgentInformation(GridPoint position) {
			super(position, true);
		}

		public String getCommunicationId() {
			return communicationId;
		}
		
		public double getSpeed() {
			return speed;
		}
		
		

		public int getHealth() {
			return health;
		}
	}
	
	public enum Behavior {
		COOPERATIVE, SELFISH, MIXED, DESTRUCTIVE
	};

}
