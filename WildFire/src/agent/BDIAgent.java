package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import agent.bdi.Extinguish;
import agent.bdi.Intention;
import agent.bdi.Patrol;
import agent.communication.info.Information;
import agent.communication.request.ActionRequest;
import agent.communication.request.InformationRequest;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestDismiss;
import agent.communication.request.RequestOffer;
import bibliothek.util.container.Tuple;
import environment.Fire;
import environment.Fire.FireInformation;
import environment.Wood;

public class BDIAgent extends ForesterAgent{
	private HashMap<Integer, Tuple<Request, Double>> openRequests;
	private HashMap<String, Request> requestedAgents; //agentID -> request
	private ActionRequest myOffer;
	
	
	public BDIAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
		this.openRequests = new HashMap<>();
		this.requestedAgents = new HashMap<String, Request>();
		this.myOffer = null;
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
			if(woodGridCells.size() != 8)
			{
				System.out.println("");
			}
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
			if(fire.getPoint().equals(currentIntention.getPosition())){
				continue;
			}
			//agents already requested
			if(agentRequested(fire.getPoint())){
				continue;
			}
			Request request = null;
			Double range = null;
			//already sent request -> increase range
			for(Tuple<Request, Double> tuple : openRequests.values()){
				if(tuple.getA().getPosition().equals(fire.getPoint())){
					request = tuple.getA();
					range = tuple.getB();
					break;
				}
			}
			if(request == null){
				request = new ActionRequest(1, fire.getPoint(), new Extinguish(), communicationId);
				range = new Double(3); // starting range
			}
			this.openRequests.put(request.getId(), new Tuple<Request,Double>(request, range));
		}
	}
	private boolean agentRequested(GridPoint p){
		for(Request request: requestedAgents.values()){
			if(request.getPosition().equals(p)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void sendAnswers() {
		for(InformationRequest infoRequest : infoRequests)
		{
			GridPoint asked = infoRequest.getPosition();
			if(asked == null) //send my position
			{
				asked = getPosition();
			}
			Information info = this.belief.getInformation(asked, infoRequest.getInformationClass());
			if(info != null && RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - info.getTimestamp() < 20){ // only "new" information
				this.costs+=communicationTool.sendInformation(info, infoRequest.getSenderID());
			}
		}
		infoRequests.clear();
		
		//Send offer
		//choose request with smallest distance (for now)
		//TODO Integrate importance: if distance difference insignificant (threshold), choose by importance
		GridPoint location = grid.getLocation(this);
		GridPoint intentionPosition = currentIntention.getPosition();
		double smallestDistance = Double.MAX_VALUE;
		if(intentionPosition != null)
		{
			smallestDistance = grid.getDistance(intentionPosition, location);
		}
		ActionRequest chosenRequest = null;
		for(ActionRequest actionRequest : actionRequests.values())
		{
			GridPoint target = actionRequest.getPosition();
			double distance = grid.getDistance(location, target);
			if(smallestDistance > distance)
			{
				smallestDistance = distance;
				chosenRequest = actionRequest;
			}
		}
		if(chosenRequest != null)
		{
			RequestOffer requestOffer = new RequestOffer(getCommunicationId(), 
					chosenRequest.getId(), smallestDistance, false);
			myOffer= chosenRequest;
			actionRequests.remove(chosenRequest);
			this.costs+=communicationTool.sendRequestOffer(chosenRequest.getSenderID(), requestOffer);
		}
		//delete old requests
		LinkedList<Integer> old =new LinkedList<Integer>();
		double currentTimestamp = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		for(Request request: actionRequests.values()){
			if(currentTimestamp-request.getTimestamp()>10){
				old.add(request.getId());
			}
		}
		actionRequests.keySet().removeAll(old);
	}
	
	@Override
	public void checkResponses() {
		//check information answers
		for(Information i:messages){
			this.belief.addInformation(i);
		}
		messages.clear();
		
		//choose best offers for each openRequest 
		HashMap<Integer, RequestOffer> bestOffers = new HashMap<Integer, RequestOffer>();
		for(RequestOffer offer: this.offers){
			RequestOffer best = bestOffers.get(offer.getRequestID());
			Request request = openRequests.get(offer.getRequestID()).getA();
			if(request!=null && !agentRequested(request.getPosition()) && (best == null || offer.getDistance()<best.getDistance())){
				bestOffers.put(request.getId(), offer);
			}
		}
		//send confirmations to best offers and add to requested agents
		for(Entry<Integer, RequestOffer> entry: bestOffers.entrySet()){
			this.costs+=communicationTool.sendRequestConfirm(entry.getValue().getSenderId(), new RequestConfirm(communicationId, entry.getValue().getRequestID()));
			Request confirmed = openRequests.get(entry.getKey()).getA();
			requestedAgents.put(entry.getValue().getSenderId(), confirmed);
			openRequests.remove(confirmed.getId());
		}
		//delete other offers
		offers.clear();
	}

	@Override
	public void doActions() {
		if (!flee()) {
			// TODO act with respect to your intention (move, extinguish, wetline, woodcutting, check weather)
			
			// Check confirmation
			if(requestConfirmation != null){
				if(currentIntention.getRequesterId()!=null){
					communicationTool.sendRequestDismiss(currentIntention.getRequesterId(), new RequestDismiss(currentIntention.getRequestId(), currentIntention.getRequesterId()));
				}
				this.currentIntention = new Intention(myOffer.getAction(), myOffer.getPosition(), null,null);
			}
			myOffer = null;
			
			//execute intention
			boolean executeSuccess = currentIntention.getAction().
					execute(this, currentIntention.getPosition());
			if(!executeSuccess){
				this.currentIntention = new Intention(new Patrol(), null, null, null);
			}
		}
	}

	@Override
	public void checkNeighbourhood() {
		updateNeighborhoodBelief();
		
		int startX = getPosition().getX();
		int startY = getPosition().getY();
		
		GridPoint nextFire = null;
		double closestDistance = Double.MAX_VALUE;
		double coldest = Double.MAX_VALUE;
		
		//Look for fire in direct neighborhood and choose coldest
		for(int offsetX = -SEEING_RANGE; offsetX<=SEEING_RANGE; offsetX++)
		{
			for(int offsetY = -SEEING_RANGE; offsetY<=SEEING_RANGE; offsetY++)
			{
				FireInformation fireInformation = belief.getInformation(
						new GridPoint(startX + offsetX, startY + offsetY), FireInformation.class);
				if(fireInformation != null && !fireInformation.isEmptyInstance())
				{
					int discreteDistance = Math.max(Math.abs(offsetX), Math.abs(offsetY));
					double heat = fireInformation.getHeat();
					if(discreteDistance < closestDistance || (discreteDistance == closestDistance && heat < coldest)){
						nextFire = fireInformation.getPosition();
						closestDistance = discreteDistance;
						coldest = heat;
					}
				}
			}
		}
		
		//change intention if better
		if(nextFire!=null && (currentIntention.getPosition()==null || grid.getDistance(currentIntention.getPosition(), getPosition())>1.5)){
			if(currentIntention.getRequesterId()!=null){
				communicationTool.sendRequestDismiss(currentIntention.getRequesterId(), new RequestDismiss(currentIntention.getRequestId(), currentIntention.getRequesterId()));
			}
			this.currentIntention = new Intention(new Extinguish(), nextFire, null,null);
		}
	}
}
