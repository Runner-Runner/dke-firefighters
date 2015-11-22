package agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import agent.bdi.Intention;
import agent.communication.CommunicationTool;
import agent.communication.info.Information;
import agent.communication.info.InformationProvider;
import agent.communication.request.ActionRequest;
import agent.communication.request.InformationRequest;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestOffer;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import statistics.DataProviderExtinguishedFireAmount;
import statistics.GraveyardStatistic;
import environment.Cloud;
import environment.Cloud.CloudInformation;
import environment.Fire;
import environment.Fire.FireInformation;
import environment.Wind;

public abstract class ForesterAgent implements InformationProvider, DataProviderExtinguishedFireAmount {
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
	protected List<ActionRequest> actionRequests;
	//other agents responds to your request
	protected List<RequestOffer> offers;
	//confirmation accepting your offer
	protected RequestConfirm requestConfirmation;
	//bounty the agent gets for extinguish fire, wetline or wood-cutting
	protected double bounty;
	//costs the agent pays for communication
	protected double costs;
	
	/**
	 * Does not have to be set. If set, represents a way for other agents 
	 * to directly communicate with this agent instance.
	 */
	protected String communicationId;
	
	//number of burning injuries it takes to kill a forester.
	protected final static int STARTING_HEALTH = 5;
	// defines the number of time steps it takes to regenerate 1 health point
	// (if injured).
	protected final static int REGENERATE_RATE = 15;

	public ForesterAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
		this.extinguishRate = extinguishRate;

		this.belief = new Belief();
		this.messages = new LinkedList<Information>();
		this.infoRequests = new LinkedList<InformationRequest>();
		this.actionRequests = new LinkedList<ActionRequest>();
		this.offers = new LinkedList<RequestOffer>();
		this.currentIntention = new Intention(null, null, null); //no initial intention
		
		this.communicationTool = new CommunicationTool(this, grid);
		
		this.communicationId = this.getClass().toString()+"|"+(agentCount++);
	}
	/**
	 * used by other agents to communicate information
	 * agent will work with them in the next iteration-step
	 * @param information
	 */
	public void receiveInformation(Information information){
		this.messages.add(information);
	}
	/**
	 * used by other agents to ask for information or help
	 * agent will handle them in the next iteration-step
	 * @param request
	 */
	public void receiveRequest(Request request){
		if(request instanceof InformationRequest)
		{
			infoRequests.add((InformationRequest)request);
		}
		else if(request instanceof ActionRequest)
		{
			actionRequests.add((ActionRequest)request);
		}
	}
	
	public void receiveConfirmation(RequestConfirm requestConfirmation){
		this.requestConfirmation = requestConfirmation;
	}
	
	public void receiveOffer(RequestOffer ro){
		this.offers.add(ro);
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 45)
	public void changeConditions(){
		// check if in burning environment
		if (isOnBurningTile()) {
			boolean lethal = burn();
			if (lethal) {
				return;
			}
		}
		regenerateTime++;
		regenerate();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 40)
	public void checkNeighbourhood(){
		updateNeighborhoodBelief();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 35)
	public abstract void doRequests();
	
	@ScheduledMethod(start = 1, interval = 1, priority = 37)
	public abstract void sendAnswers();
	
	@ScheduledMethod(start = 1, interval = 1, priority = 30)
	public abstract void checkResponds();
	
	@ScheduledMethod(start = 1, interval = 1, priority = 20)
	public abstract void doActions();

	/**
	 * Extinguish fire in one grid space. Time-consuming action: takes up one
	 * time step
	 * 
	 * @param pt
	 */
	public boolean extinguishFire(GridPoint pt) {
		GridPoint position = grid.getLocation(this);

		// check if fire position really is in the Moore neighborhood
		if (Math.abs(position.getX() - pt.getX()) > 1
				|| Math.abs(position.getY() - pt.getY()) > 1) {
			// illegal action
			return false;
		}

		Iterable<Object> objects = grid.getObjectsAt(pt.getX(), pt.getY());
		Fire fire = null;
		for (Object obj : objects) {
			if (obj instanceof Fire) {
				fire = (Fire) obj;
				break;
			}
		}
		if (fire == null) {
			// no fire to extinguish
			FireInformation extinguished = new FireInformation(pt.getX(), pt.getY());
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
		Context<Object> context = ContextUtils.getContext(this);
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
					belief.addInformation(new CloudInformation(startX + xOffset, startY + yOffset));
				}
			}
		}
	}

	/***
	 * moves agent to a further away GridPoint in an angle to it, whereby max distance is its speed
	 * (uses the center of the grids (e.g. 2.5| 3.5))
	 * @param pt target gridpoint
	 */
	protected void moveTowards(GridPoint pt) {
		if(pt.getX()<0||pt.getY()<0||pt.getX()>=grid.getDimensions().getWidth()||pt.getY()>=grid.getDimensions().getHeight()){
			return;
		}
		//current grid position
		GridPoint oldPos = grid.getLocation(this);
		
		//angle of both tiles
		double angle = getAngle(oldPos, pt);
		//temporary speed
		double tempSpeed = speed;
		//if agent has been moved
		boolean moved = false;
		//distance of both tiles
		double distance = grid.getDistance(oldPos, pt);
		
		if(distance>speed){
			while(!moved && tempSpeed > 0)
			{
				// position of step tile
				double x = oldPos.getX()+0.5 + tempSpeed * Math.cos(angle);
				double y = oldPos.getY()+0.5 + tempSpeed * Math.sin(angle);
				moved = moveTo(new GridPoint((int)x,(int)y));
	
				if(!moved){
					--tempSpeed;
				}
			}
		}else{
			//TODO implement not reaching the target point even now because another agent is on this tile
			//if pt is reachable in one time step
			moveTo(pt);
		}
	}

	/***
	 * move agent to GridPoint and test if it is already occupied
	 * 
	 * @param pt
	 * @return if the move worked
	 */
	private boolean moveTo(GridPoint pt) {
		if( pt.equals(grid.getLocation(this))){
			return true;
		}
			
		boolean occupied = false;
		for (Object o : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (o instanceof ForesterAgent) {
				occupied = true;
				break;
			}
		}

		if (!occupied) {
			space.moveTo(this, pt.getX() + 0.5, pt.getY() + 0.5);
			grid.moveTo(this, pt.getX(), pt.getY());
		}
		return !occupied;
	}

	/***
	 * calculates angle of two grids from the center
	 * @param from
	 * @param to
	 * @return angle
	 */
	private double getAngle(GridPoint from, GridPoint to) {
		NdPoint fromPt = new NdPoint(from.getX() + 0.5, from.getY() + 0.5);
		NdPoint toPt = new NdPoint(to.getX() + 0.5, to.getY() + 0.5);
		return SpatialMath.calcAngleFor2DMovement(space, fromPt, toPt);
	}

	public boolean isOnBurningTile() {
		GridPoint location = grid.getLocation(this);
		Iterable<Object> objects = grid.getObjectsAt(location.getX(),
				location.getY());
		for (Object obj : objects) {
			if (obj instanceof Fire) {
				return true;
			}
		}
		return false;
	}

	/**

	 * Updates belief about fire in Moore neighborhood. This action does not require a time step.
	 * 
	 * @return All information that actually changed the belief
	 */
	protected List<Information> updateNeighborhoodBelief() {
		List<Information> informationList = new ArrayList<>();

		// get information about fire
		GridPoint location = grid.getLocation(this);
		int startX = location.getX() - 1;
		int startY = location.getY() - 1;
		for (int xOffset = 0; xOffset < 3; xOffset++) {
			if (startX + xOffset < 0
					|| startX + xOffset == grid.getDimensions().getWidth()) {
				continue;
			}
			for (int yOffset = 0; yOffset < 3; yOffset++) {
				if (startY + yOffset < 0
						|| startY + yOffset == grid.getDimensions().getHeight()) {
					continue;
				}

				Iterable<Object> gridObjects = grid.getObjectsAt(startX
						+ xOffset, startY + yOffset);
				boolean foundFire = false;
				boolean foundAgent = false;

				for(Object obj : gridObjects)
				{
					if(obj instanceof Fire || obj instanceof ForesterAgent)
					{
						Information information = ((InformationProvider)obj).getInformation();
						boolean changed = belief.addInformation(information);
						if(changed)
						{
							informationList.add(information);
						}
						
						if(obj instanceof Fire)
						{
							foundFire = true;
						}
						if(obj instanceof ForesterAgent)
						{
							foundAgent = true;
						}
					}
				}
				if (!foundFire) {
					FireInformation removeInformation = new FireInformation(
							startX + xOffset, startY + yOffset);
					boolean changed = belief.addInformation(removeInformation);
					if(changed)
					{
						informationList.add(removeInformation);
					}
				}
				if(!foundAgent)
				{
					AgentInformation removeInformation = new AgentInformation(
							startX + xOffset, startY + yOffset);
					informationList.add(removeInformation);
					boolean changed = belief.addInformation(removeInformation);
					if(changed)
					{
						informationList.add(removeInformation);
					}
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
		
		Context<Object> context = ContextUtils.getContext(this);
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
	
	public String getCommunicationId()
	{
		return communicationId;
	}
	
	public void setCommunicationId(String communicationId) 
	{
		this.communicationId = communicationId;
	}
	
	@Override
	public AgentInformation getInformation() {
		GridPoint location = grid.getLocation(this);
		return new AgentInformation(location.getX(), location.getY(), speed, health, currentIntention.getxPosition(), currentIntention.getyPosition());
	}

	public static class AgentInformation extends Information {

		private double speed;
		private int health;

		private Integer intentionX;
		private Integer intentionY;
		
		private AgentInformation(Integer positionX, Integer positionY, double speed, int health, Integer intentionX, Integer intentionY) {
			super(positionX, positionY);
			this.speed = speed;
			this.health = health;
			this.intentionX = intentionX;
			this.intentionY = intentionY;
		}

		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public AgentInformation(Integer positionX, Integer positionY) {
			super(positionX, positionY, true);
		}

		public double getSpeed() {
			return speed;
		}

		
		public Integer getIntentionX() {
			return intentionX;
		}

		public Integer getIntentionY() {
			return intentionY;
		}

		public int getHealth() {
			return health;
		}
	}
	
	public enum Behavior {
		COOPERATIVE, SELFISH, MIXED, DESTRUCTIVE
	};

}
