package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import agent.bdi.Extinguish;
import agent.communication.CommunicationTool;
import agent.communication.info.Information;
import agent.communication.request.ActionRequest;
import agent.communication.request.InformationRequest;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestDismiss;
import agent.communication.request.RequestOffer;
import environment.Fire;
import environment.Wood;

public class BDIAgent extends ForesterAgent{
	private HashMap<Request, Integer> openRequests; //request -> range
	private HashMap<Integer, Request> openIDs; //requestID -> request
	private HashMap<String, Request> requestedAgents; //agentID -> request
	
	
	public BDIAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
		this.openRequests = new HashMap<Request, Integer>();
		this.openIDs = new HashMap<Integer, Request>();
		this.requestedAgents = new HashMap<String, Request>();
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
		
		//check dismiss (if other agent changed intention) Other dismiss means your intention is obsolete
		LinkedList<RequestDismiss> handeled = new LinkedList<RequestDismiss>();
		for(RequestDismiss rd: this.rejections){
			Request request = this.requestedAgents.get(rd.getSenderID());
			if(request.getId() == rd.getRequestID()){
				this.requestedAgents.remove(rd.getSenderID());
				handeled.add(rd);
			}
		}
		this.rejections.removeAll(handeled);
		
		//check if you need help
		GridPoint location = grid.getLocation(this);
		
		GridCellNgh<Fire> nghFire = new GridCellNgh<>(grid, location,
				Fire.class, 1, 1);
		List<GridCell<Fire>> fires = nghFire.getNeighborhood(true);
		
		for(GridCell<Fire> fire: fires){
			//my intention
			if(fire.getPoint().getX() == currentIntention.getxPosition() && currentIntention.getyPosition() == fire.getPoint().getY()){
				continue;
			}
			//agents already requested
			if(agentRequested(fire.getPoint().getX(), fire.getPoint().getY())){
				continue;
			}
			Request request = null;
			Integer range = null;
			//already sent request -> increase range
			for(Request r: openRequests.keySet()){
				if(r.getPositionX() == fire.getPoint().getX() && r.getPositionY() == fire.getPoint().getY()){
					request = r;
					range = openRequests.get(r);
					break;
				}
			}
			if(request == null){
				request = new ActionRequest(1, fire.getPoint().getX(), fire.getPoint().getY(), new Extinguish(), communicationId);
				range = new Integer(3); // starting range
				this.openRequests.put(request, range);
			}
			else{
				this.openRequests.put(request, ++range);
			}
			
		}
	}
	private boolean agentRequested(int x, int y){
		for(Request request: requestedAgents.values()){
			if(request.getPositionX() == x && request.getPositionY()== y){
				return true;
			}
		}
		return false;
	}

	@Override
	public void sendAnswers() {
		for(InformationRequest infoRequest : infoRequests)
		{
			Information info = this.belief.getInformation(infoRequest.getPositionX(), infoRequest.getPositionY(), infoRequest.getInformation());
			if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - info.getTimestamp() < 20){ // only "new" information
				this.costs+=communicationTool.sendInformation(info, infoRequest.getSenderID());
			}
		}
		infoRequests.clear();
		
		//Send offer
		//choose request with lowest distance (for now)
		//TODO Integrate importance: if distance difference insignificant (threshold), choose by importance
		GridPoint location = grid.getLocation(this);
		double lowestDistance = Double.MAX_VALUE;
		ActionRequest chosenRequest = null;
		for(ActionRequest actionRequest : actionRequests)
		{
			GridPoint target = new GridPoint(actionRequest.getPositionX(), actionRequest.getPositionY());
			double distance = CommunicationTool.calculateDistance(location, target);
			if(lowestDistance > distance)
			{
				lowestDistance = distance;
				chosenRequest = actionRequest;
			}
		}
		if(chosenRequest != null)
		{
			RequestOffer requestOffer = new RequestOffer(chosenRequest.getSenderID(), 
					chosenRequest.getId(), lowestDistance, false);
			this.costs+=communicationTool.sendRequestOffer(chosenRequest.getSenderID(), requestOffer);
		}
		
	}
	
	@Override
	public void checkResponds() {
		//check information answers
		for(Information i:messages){
			this.belief.addInformation(i);
		}
		messages.clear();
		
		//choose best offers for each openRequest 
		HashMap<Integer, RequestOffer> bestOffers = new HashMap<Integer, RequestOffer>();
		for(RequestOffer offer: this.offers){
			RequestOffer best = bestOffers.get(offer.getRequestID());
			Request request = openIDs.get(offer.getRequestID());
			if(request!=null && !agentRequested(request.getPositionX(), request.getPositionY()) && (best == null || offer.getDistance()<best.getDistance())){
				bestOffers.put(request.getId(), offer);
			}
		}
		//send confirmations to best offers and add to requested agents
		for(Entry<Integer, RequestOffer> entry: bestOffers.entrySet()){
			this.costs+=communicationTool.sendRequestConfirm(entry.getValue().getSenderId(), new RequestConfirm(communicationId, entry.getValue().getRequestID()));
			requestedAgents.put(entry.getValue().getSenderId(), openIDs.get(entry.getKey()));
			Request removed = openIDs.remove(entry.getKey());
			openRequests.remove(removed);
			
		}
		//delete other offers
		offers.clear();
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
				
				requestConfirmation = null;
			}
			
			// go for a walk
			GridPoint location = grid.getLocation(this);
			Random r = new Random();
			int x = location.getX() + r.nextInt(3) - 1;
			int y = location.getY() + r.nextInt(3) - 1;
			moveTowards(new GridPoint(x, y));
		}
	}
}
