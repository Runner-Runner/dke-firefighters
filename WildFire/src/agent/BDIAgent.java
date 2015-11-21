package agent;

import java.util.List;
import java.util.Random;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import environment.Fire;
import environment.Fire.FireInformation;
import environment.Wood;

public class BDIAgent extends ForesterAgent{

	public BDIAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
	}

	@Override
	protected void decideOnActions() {
		updateNeighborhoodBelief(); 
		checkMessages();
		checkIntention();
		handleRequests();
		doAction();
		checkAskForHelp();
	}

	private void checkIntention() {
		if(currentIntention.getAction() instanceof Extinguish){
			FireInformation fi = belief.getInformation(currentIntention.getxPosition(), currentIntention.getyPosition(),FireInformation.class);
			if(fi == null || fi.isEmptyInstance()){
				currentIntention = new Intention(null, null, null);
			}
		}
	}

	private void checkAskForHelp() {
		GridPoint location = grid.getLocation(this);
		
		GridCellNgh<Fire> nghFire = new GridCellNgh<>(grid, location,
				Fire.class, 1, 1);
		List<GridCell<Fire>> fires = nghFire.getNeighborhood(true);
		
		GridCellNgh<ForesterAgent> nghAgents = new GridCellNgh<>(grid, location,
				ForesterAgent.class, 1, 1);
		List<GridCell<ForesterAgent>>  agents = nghAgents.getNeighborhood(true);
		
		//there are more fires than agents -> call 911
		if(fires.size()>agents.size()-1){
			communicationTool.setSendingRange(100);
			this.costs+=communicationTool.sendRequest(new ActionRequest(1, location.getX(), location.getY(), new Extinguish(), this.communicationId));
		}
	}

	private void doAction() {
		if(!flee()){
			if(currentIntention.getAction()!=null){
				GridPoint gp = new GridPoint(currentIntention.getxPosition(),currentIntention.getyPosition());
				if(!currentIntention.getAction().execute(this, gp)){
					moveTowards(gp);
				}
			}
			else{
				noIntention();
			}
		}
	}
	private void noIntention(){
		GridPoint location = grid.getLocation(this);
		//look for nearest fire in beliefs
		Intention intention = null;
		double intentionDistance = Double.MAX_VALUE;
		for(FireInformation fi:this.belief.getAllInformation(FireInformation.class)){
			if(!fi.isEmptyInstance()){
			double distance = calculateDistance(fi.getPositionX(), fi.getPositionY(), location.getX(), location.getY());
				if(distance<intentionDistance){
					intentionDistance = distance;
					intention = new Intention(new Extinguish(), fi.getPositionX(), fi.getPositionY());
				}
			}
		}
		if(intention != null){
			this.currentIntention = intention;
			doAction();
		}
		else{
			//go for a walk
			//TODO no random, make a plan
			Random r = new Random();
			int x = location.getX()+r.nextInt(3)-1;
			int y = location.getY()+r.nextInt(3)-1;
			moveTowards(new GridPoint(x,y));
		}
	}
	private boolean flee(){
		GridPoint location = grid.getLocation(this);
		if(isOnBurningTile())
		{
			GridPoint fleeingPoint = null;
			//move to burned wood tile (secure space) if possible
			GridCellNgh<Wood> nghWoodCreator = new GridCellNgh<>(grid, location,
					Wood.class, 1, 1);
			List<GridCell<Wood>> woodGridCells = nghWoodCreator.getNeighborhood(false);
			for(GridCell<Wood> cell : woodGridCells)
			{
				if (cell.size() == 0) {
					fleeingPoint = cell.getPoint();
					break;
				}
			}
			if(fleeingPoint == null)
			{
				//otherwise, move to first non-burning tile
				GridCellNgh<Fire> nghFireCreator = new GridCellNgh<>(grid, location,
						Fire.class, 1, 1);
				List<GridCell<Fire>> fireGridCells = nghFireCreator.getNeighborhood(false);
				for (GridCell<Fire> cell : fireGridCells) {
					if (cell.size() == 0) {
						fleeingPoint = cell.getPoint();
						break;
					}
				}
				
				// all neighbor tiles on fire - move to first one
				if(fleeingPoint == null)
				{
					fleeingPoint = fireGridCells.get(0).getPoint();
				}
			}
			return true;
		}
		return false;
	}

	private void handleRequests() {
		double actualTime = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for(Request r:requests){
			if(r instanceof InformationRequest){
				InformationRequest req = (InformationRequest)r;
				Information myInfo = this.belief.getInformation(r.getPositionX(), r.getPositionY(), req.getInformation());
				if(actualTime - myInfo.getTimestamp()< 20){ //comunicate only "new" information
					this.communicationTool.sendInformation(myInfo, req.getSenderID());
				}
			}
			else if(r instanceof ActionRequest){
				GridPoint me = grid.getLocation(this);
				ActionRequest ar = (ActionRequest)r;
				double distance = calculateDistance(me.getX(), me.getY(), ar.getPositionX(),ar.getPositionY());
				//distance is shorter as current intention -> new intention TODO check importance
				if(this.currentIntention.getAction()==null || calculateDistance(currentIntention.getxPosition(), currentIntention.getyPosition(), me.getX(), me.getY())>distance){
					this.currentIntention = new Intention(ar.getAction(), ar.getPositionX(), ar.getPositionY());
					//TODO send "I will help" (agentInformation with current desire)
				}
			}
		}
		//TODO What if another agent is even closer? Should the other agent 
		//send back a confirmation and only then this intention is changed?
		requests.clear();
	}
	private double calculateDistance(int x1, int y1, int x2, int y2){
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));
	}

	private void checkMessages() {
		for(Information i:messages){
			this.belief.addInformation(i);
		}
		messages.clear();
	}
}
