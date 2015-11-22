package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import agent.bdi.Extinguish;
import agent.bdi.Intention;
import agent.bdi.Patrol;
import agent.communication.CommunicationTool;
import agent.communication.info.Information;
import agent.communication.request.ActionRequest;
import agent.communication.request.InformationRequest;
import agent.communication.request.Request;
import agent.communication.request.RequestOffer;
import environment.Fire;
import environment.Wood;

public class BDIAgent extends ForesterAgent{
	private int increasingRange;
	private List<Request> myRequest;
	private HashMap<String, String> requestedAgents; //agentID -> requestID
	
	
	public BDIAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
		this.increasingRange = 3;
		this.myRequest = new LinkedList<Request>();
		this.requestedAgents = new HashMap<String, String>();
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
			moveTowards(fleeingPoint);
			return true;
		}
		return false;
	}

	@Override
	public void doRequests() {
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

	@Override
	public void sendAnswers() {
		for(InformationRequest infoRequest : infoRequests)
		{
			//TODO Send information answers
		}
		
		//Send offer
		//choose request with smallest distance (for now)
		//TODO Integrate importance: if distance difference insignificant (threshold), choose by importance
		GridPoint location = grid.getLocation(this);
		double smallestDistance = Double.MAX_VALUE;
		ActionRequest chosenRequest = null;
		for(ActionRequest actionRequest : actionRequests.values())
		{
			GridPoint target = new GridPoint(actionRequest.getPositionX(), actionRequest.getPositionY());
			double distance = CommunicationTool.calculateDistance(location, target);
			if(smallestDistance > distance)
			{
				smallestDistance = distance;
				chosenRequest = actionRequest;
			}
		}
		if(chosenRequest == null)
		{
			//no request -> no offer
			return;
		}
		
		RequestOffer requestOffer = new RequestOffer(getCommunicationId(), 
				chosenRequest.getId(), smallestDistance, false);
		communicationTool.sendRequestOffer(chosenRequest.getSenderID(), requestOffer);
	}
	
	@Override
	public void checkResponds() {
		//check information answers
		for(Information i:messages){
			this.belief.addInformation(i);
		}
		messages.clear();
		
		//TODO check offers and send confirmations
	}

	@Override
	public void doActions() {
		if (!flee()) {
			// TODO act with respect to your intention (move, extinguish, wetline, woodcutting, check weather)
			
			// Check confirmation
			if(requestConfirmation != null)
			{
				int requestID = requestConfirmation.getRequestID();
				//TODO set request action as intention
				ActionRequest confirmedRequest = actionRequests.get(requestID);
				if(confirmedRequest != null)
				{
					currentIntention = new Intention(confirmedRequest.getAction(), 
							confirmedRequest.getPositionX(),  
							confirmedRequest.getPositionY(), confirmedRequest.getSenderID());
				}
				requestConfirmation = null;
			}
			
			//TODO derive other more urgent intentions from environment?
			
			if(currentIntention == null)
			{
				currentIntention = new Intention(new Patrol(), null, null, null);
			}
			
			//execute intention
			boolean executeSuccess = currentIntention.getAction().
					execute(this, grid.getLocation(this));
			//TODO Remove intention if !executeSuccess
		}
	}
}
