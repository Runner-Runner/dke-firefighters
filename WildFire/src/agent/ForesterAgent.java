package agent;

import java.util.ArrayList;
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

public abstract class ForesterAgent {
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	
	//in distance per step
	protected double speed;
	//rate at which the fire heat is lowered when extinguishing
	protected double extinguishRate;
	//defines the number of time steps this forester experienced burning injuries
	protected int health = STARTING_HEALTH;
	//how many time steps until the last burning injury
	private int regenerateTime = 0;
	//how much fire this agent extinguished so far
	private double extinguishedFireAmount = 0;
	
	protected Knowledge knowledge;
	
	protected CommunicationTool communicationTool;
	
	//number of burning injuries it takes to kill a forester.
	protected final static int STARTING_HEALTH = 5;
	//defines the number of time steps it takes to regenerate 1 health point (if injured).
	protected final static int REGENERATE_RATE = 7;
	
	public ForesterAgent(ContinuousSpace<Object> space, Grid<Object> grid, double speed, double extinguishRate) {
		this.space = space;
		this.grid = grid;
		this.speed = speed;
		this.extinguishRate = extinguishRate;
		knowledge = new Knowledge();
		
		communicationTool = new CommunicationTool(this, grid);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		//check if in burning environment
		if(isOnBurningTile())
		{
			burn();
		}
		regenerate();
		
		decideOnActions();
	}
	
	protected abstract void decideOnActions();
	
	/**
	 * Extinguish fire in one grid space. Time-consuming action: takes up one time step
	 * 
	 * @param pt
	 */
	protected void extinguishFire(GridPoint pt)
	{
		GridPoint position = grid.getLocation(this);
		
		//check if fire position really is in the Moore neighborhood
		if(Math.abs(position.getX()-pt.getX()) > 1 || Math.abs(position.getY()-pt.getY()) > 1)
		{
			//illegal action
			return;
		}
			
		Iterable<Object> objects = grid.getObjectsAt(pt.getX(), pt.getY());
		Fire fire = null;
		for(Object obj : objects)
		{
			if(obj instanceof Fire)
			{
				fire = (Fire)obj;
				break;
			}
		}
		if(fire == null)
		{
			//no fire to extinguish
			return;
		}
		
		extinguishedFireAmount += fire.decreaseHeat(extinguishRate);
	}
	
	/**
	 * Obtain information about the weather conditions in the Moore neighborhood
	 */
	protected void checkWeather()
	{
		//TODO send newly obtained information to (nearby?) agents
		
		//get information about wind 
		Context<Object> context = ContextUtils.getContext(this);
		IndexedIterable<Object> windObjects = context.getObjects(Wind.class);
		Wind wind = (Wind)windObjects.get(0);
		knowledge.setWindInformation(wind.getInformation());
		
		//get information about clouds
		GridPoint location = grid.getLocation(this);
		int startX = location.getX() - 1;
		int startY = location.getY() - 1;
		for(int xOffset=0; xOffset<3; xOffset++)
		{
			for(int yOffset=0; yOffset<3; yOffset++)
			{
				Iterable<Object> gridObjects = grid.getObjectsAt(startX + xOffset, startY + yOffset);
				boolean foundCloud = false;
				for(Object obj : gridObjects)
				{
					if(obj instanceof Cloud)
					{
						Cloud cloud = (Cloud)obj;
						knowledge.getCloudInformationMap().addInformation(cloud.getInformation());
						foundCloud = true;
						break;
					}
				}
				if(!foundCloud)
				{
					knowledge.getCloudInformationMap().addInformation(
						new CloudInformation(startX + xOffset, startY + yOffset));
				}
			}
		}
	}
	
	protected void moveTowards(GridPoint pt) {
		//get actual point
		NdPoint myPoint = space.getLocation(this);
		//cast to continuous point
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		//calculate angle to move
		double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint);
		//move in continuous space
		space.moveByVector(this, speed, angle, 0);
		myPoint = space.getLocation(this);

		//move in grid
		grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
	}

	public boolean isOnBurningTile()
	{
		GridPoint location = grid.getLocation(this);
		Iterable<Object> objects = grid.getObjectsAt(location.getX(), location.getY());
		for(Object obj : objects)
		{
			if(obj instanceof Fire)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Updates knowledge about fire in Moore neighborhood. This action does not require a time step.
	 * 
	 * @return All information that actually changed the knowledge
	 */
	protected List<FireInformation> updateFireKnowledge() {
		List<FireInformation> fireInformationList = new ArrayList<FireInformation>();
		
		//get information about fire
		GridPoint location = grid.getLocation(this);
		int startX = location.getX() - 1;
		int startY = location.getY() - 1;
		for(int xOffset=0; xOffset<3; xOffset++)
		{
			if(startX + xOffset < 0 || startX + xOffset == grid.getDimensions().getWidth())
			{
				continue;
			}
			for(int yOffset=0; yOffset<3; yOffset++)
			{
				if(startY + yOffset < 0 || startY + yOffset == grid.getDimensions().getHeight())
				{
					continue;
				}
				
				Iterable<Object> gridObjects = grid.getObjectsAt(startX + xOffset, startY + yOffset);
				boolean foundFire = false;
				for(Object obj : gridObjects)
				{
					if(obj instanceof Fire)
					{
						FireInformation fireInformation = ((Fire)obj).getInformation();
						boolean changed = knowledge.getFireInformationMap().addInformation(fireInformation);
						if(changed)
						{
							fireInformationList.add(fireInformation);
							foundFire = true;
							break;
						}
					}
				}
				if(!foundFire)
				{
					FireInformation removeInformation = new FireInformation(startX + xOffset, startY + yOffset);
					fireInformationList.add(removeInformation);
					knowledge.getFireInformationMap().addInformation(removeInformation);
				}
			}
		}
		return fireInformationList;
	}
	
	protected void burn()
	{
		health--;
		if(health <= 0)
		{
			//die
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
		}
		regenerateTime = 0;
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
	
	public Knowledge getKnowledge()
	{
		return knowledge;
	}
	
	public CommunicationTool getCommunicationTool()
	{
		return communicationTool;
	}
	
	public double getExtinguishedFireAmount()
	{
		return extinguishedFireAmount;
	}
	
	public enum Behavior {
		COOPERATIVE, SELFISH, MIXED, DESTRUCTIVE
	};
}
