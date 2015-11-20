package agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
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
<<<<<<< HEAD

	protected Knowledge knowledge;

	protected CommunicationTool communicationTool;

	// number of burning injuries it takes to kill a forester.
=======
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
	//agent can decide to help/answer
	protected List<Request> requests;
	//bounty the agent gets for extinguish fire, wetline or wood-cutting
	protected double bounty;
	//costs the agent pays for communication
	protected double costs;
	
	/**
	 * Does not have to be set. If set, represents a way for other agents 
	 * to directly communicate with this agent instance.
	 */
	private String communicationId;
	
	//number of burning injuries it takes to kill a forester.
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
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
<<<<<<< HEAD
		knowledge = new Knowledge();

		communicationTool = new CommunicationTool(this, grid);
	}

=======
		this.belief = new Belief();
		this.messages = new LinkedList<Information>();
		this.requests = new LinkedList<Request>();
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
		this.requests.add(request);
	}
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// check if in burning environment
		if (isOnBurningTile()) {
			boolean lethal = burn();
			if (lethal) {
				return;
			}
		}
		regenerateTime++;
		regenerate();

		decideOnActions();
	}

	protected abstract void decideOnActions();

	/**
	 * Extinguish fire in one grid space. Time-consuming action: takes up one
	 * time step
	 * 
	 * @param pt
	 */
	protected void extinguishFire(GridPoint pt) {
		GridPoint position = grid.getLocation(this);

		// check if fire position really is in the Moore neighborhood
		if (Math.abs(position.getX() - pt.getX()) > 1
				|| Math.abs(position.getY() - pt.getY()) > 1) {
			// illegal action
			return;
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
			return;
		}

		extinguishedFireAmount += fire.decreaseHeat(extinguishRate);
	}

	/**
	 * Obtain information about the weather conditions in the Moore neighborhood
	 */
	protected void checkWeather() {
		// TODO send newly obtained information to (nearby?) agents

		// get information about wind
		Context<Object> context = ContextUtils.getContext(this);
		IndexedIterable<Object> windObjects = context.getObjects(Wind.class);
<<<<<<< HEAD
		Wind wind = (Wind) windObjects.get(0);
		knowledge.setWindInformation(wind.getInformation());

		// get information about clouds
=======
		Wind wind = (Wind)windObjects.get(0);
		belief.setWindInformation(wind.getInformation());
		
		//get information about clouds
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
		GridPoint location = grid.getLocation(this);
		int startX = location.getX() - 1;
		int startY = location.getY() - 1;
		for (int xOffset = 0; xOffset < 3; xOffset++) {
			for (int yOffset = 0; yOffset < 3; yOffset++) {
				Iterable<Object> gridObjects = grid.getObjectsAt(startX
						+ xOffset, startY + yOffset);
				boolean foundCloud = false;
<<<<<<< HEAD
				for (Object obj : gridObjects) {
					if (obj instanceof Cloud) {
						Cloud cloud = (Cloud) obj;
						knowledge.addInformation(cloud.getInformation());
=======
				for(Object obj : gridObjects)
				{
					if(obj instanceof Cloud)
					{
						Cloud cloud = (Cloud)obj;
						belief.addInformation(cloud.getInformation());
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
						foundCloud = true;
						break;
					}
				}
<<<<<<< HEAD
				if (!foundCloud) {
					knowledge.addInformation(new CloudInformation(startX
							+ xOffset, startY + yOffset));
=======
				if(!foundCloud)
				{
					belief.addInformation(new CloudInformation(startX + xOffset, startY + yOffset));
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
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
	protected boolean moveTo(GridPoint pt) {
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
<<<<<<< HEAD
	 * Updates knowledge about fire in Moore neighborhood. This action does not
	 * require a time step.
=======
	 * Updates belief about fire in Moore neighborhood. This action does not require a time step.
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
	 * 
	 * @return All information that actually changed the belief
	 */
	protected List<FireInformation> updateFireBelief() {
		List<FireInformation> fireInformationList = new ArrayList<FireInformation>();

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
<<<<<<< HEAD
				for (Object obj : gridObjects) {
					if (obj instanceof Fire) {
						FireInformation fireInformation = ((Fire) obj)
								.getInformation();
						boolean changed = knowledge
								.addInformation(fireInformation);
						if (changed) {
=======
				for(Object obj : gridObjects)
				{
					if(obj instanceof Fire)
					{
						FireInformation fireInformation = ((Fire)obj).getInformation();
						boolean changed = belief.addInformation(fireInformation);
						if(changed)
						{
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
							fireInformationList.add(fireInformation);
							foundFire = true;
							break;
						}
					}
				}
				if (!foundFire) {
					FireInformation removeInformation = new FireInformation(
							startX + xOffset, startY + yOffset);
					fireInformationList.add(removeInformation);
					belief.addInformation(removeInformation);
				}
			}
		}
		return fireInformationList;
	}

	/**
	 * Returns true if the agent died from burning injuries.
	 * 
	 * @return
	 */
	protected boolean burn() {
		regenerateTime = 0;
		health--;
<<<<<<< HEAD
		if (health <= 0) {
			// die
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
=======
		if(health <= 0)
		{
			die();
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
			return true;
		}
		return false;
	}
<<<<<<< HEAD

	protected void regenerate() {
		if (regenerateTime % REGENERATE_RATE == 0) {
			if (health < STARTING_HEALTH) {
=======
	
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
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
				health++;
			}
		}
	}
<<<<<<< HEAD

	public Knowledge getKnowledge() {
		return knowledge;
	}

	public CommunicationTool getCommunicationTool() {
		return communicationTool;
	}

	public double getExtinguishedFireAmount() {
		return extinguishedFireAmount;
	}

	public enum Behavior {
		COOPERATIVE, SELFISH, MIXED, DESTRUCTIVE
	};

	@Override
	public AgentInformation getInformation() {
		GridPoint location = grid.getLocation(this);
		return new AgentInformation(location.getX(), location.getY(), speed,
				health);
=======
	
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
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
	}

	public static class AgentInformation extends Information {

		private double speed;
		private int health;
<<<<<<< HEAD

		private AgentInformation(Integer positionX, Integer positionY,
				double speed, int health) {
=======
		private Integer intentionX;
		private Integer intentionY;
		
		private AgentInformation(Integer positionX, Integer positionY, double speed, int health, Integer intentionX, Integer intentionY) {
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3
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
<<<<<<< HEAD
=======
		
		public Integer getIntentionX() {
			return intentionX;
		}

		public Integer getIntentionY() {
			return intentionY;
		}
>>>>>>> fb59c7ac0cb978630e261b1eb0089170882562a3

		public int getHealth() {
			return health;
		}
	}
	
	public enum Behavior {
		COOPERATIVE, SELFISH, MIXED, DESTRUCTIVE
	};

}
