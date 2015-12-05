package agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import main.Pair;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
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
import environment.Fire;
import environment.Fire.FireInformation;
import environment.Wood;

public class BDIAgent extends ForesterAgent{
	private HashMap<Integer, Pair<ActionRequest, Double>> openRequests;
	private HashMap<String, ActionRequest> requestedAgents; //agentID -> request
	private ActionRequest myOffer;
	
	
	public BDIAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
		this.openRequests = new HashMap<>();
		this.requestedAgents = new HashMap<String, ActionRequest>();
		this.myOffer = null;
	}


	
	private boolean flee(){
		GridPoint location = grid.getLocation(this);
		Fire fire = isOnBurningTile();
		if(fire!=null)
		{
			//if little fire -> extinguish
			if(fire.getHeat()<=extinguishRate){
				extinguishFire(location);
			}
			//else run away
			else{
				NdPoint exact = getExactPosition();
				GridPoint fleeingPoint = null;
				double fleeingDistance = Double.MAX_VALUE;
				//move to burned wood tile (secure space) if possible
				GridCellNgh<Wood> nghWoodCreator = new GridCellNgh<>(grid, location,
						Wood.class, 1, 1);
				List<GridCell<Wood>> woodGridCells = nghWoodCreator.getNeighborhood(false);
				for(GridCell<Wood> cell : woodGridCells)
				{
					if (cell.size() == 0) {
						double distance = Math.sqrt(Math.pow(exact.getX()-cell.getPoint().getX(), 2)+Math.pow(exact.getY()-cell.getPoint().getY(), 2));
						if(distance<fleeingDistance){
							fleeingPoint = cell.getPoint();
							fleeingDistance = distance;
						}
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
							double distance = Math.sqrt(Math.pow(exact.getX()-cell.getPoint().getX(), 2)+Math.pow(exact.getY()-cell.getPoint().getY(), 2));
							if(distance<fleeingDistance){
								fleeingPoint = cell.getPoint();
								fleeingDistance = distance;
							}
						}
					}
					
				}
				// all neighbor tiles on fire - try to extinguish
				if(fleeingPoint == null)
				{
					extinguishFire(location);
				}
				else
				{
					moveTowards(fleeingPoint);
				}
			}
			return true;
		}
		return false;
	}
	private void changeIntention(Intention newIntention)
	{
		for(Entry<Integer, String> entry : currentIntention.getRequester().entrySet()){
			communicationTool.sendRequestDismiss(entry.getValue(), new RequestDismiss(entry.getKey(), communicationId));
		}
		this.currentIntention = newIntention;
	}
	@Override
	public void checkNeighbourhood() {
		updateNeighborhoodBelief();
		//check if actual intention is obsolete
		if(currentIntention.getAction() instanceof Extinguish)
		{
			FireInformation fi = belief.getInformation(currentIntention.getPosition(), FireInformation.class);
			if(fi==null || fi.isEmptyInstance())
			{
				changeIntention(new Intention(new Patrol(), null, null, null));
			}
		}
		
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
		if(nextFire!=null && (currentIntention.getPosition()==null || grid.getDistance(currentIntention.getPosition(), getPosition())>grid.getDistance(getPosition(), nextFire))){
			changeIntention(new Intention(new Extinguish(), nextFire, null,null));
		}


	}
	@Override
	public void doRequests() {
		//check dismiss 
		for(RequestDismiss rd: this.rejections){
			//check if your intention is obsolete
			if(currentIntention.removeRequester(rd.getRequestID())){
				changeIntention(new Intention(new Patrol(), null, null, null));
			}
			//requested agent found better fire
			else{
				Request request = this.requestedAgents.get(rd.getSenderID());
				if(request != null && request.getId() == rd.getRequestID()){
					this.requestedAgents.remove(rd.getSenderID());
				}				
			}
		}
		this.rejections.clear();
		
		//check if you need help
		GridPoint location = grid.getLocation(this);
		
		GridCellNgh<Fire> nghFire = new GridCellNgh<>(grid, location,
				Fire.class, 1, 1);
		List<GridCell<Fire>> fires = nghFire.getNeighborhood(true);
		
		for(GridCell<Fire> fire: fires){
			if(fire.size()>0){
				//my intention
				if(fire.getPoint().equals(currentIntention.getPosition())){
					continue;
				}
				//agents already requested
				if(agentRequested(fire.getPoint())!=null){
					continue;
				}
				ActionRequest request = null;
				Double range = null;
				//already sent request -> increase range
				for(Pair<ActionRequest, Double> tuple : openRequests.values()){
					if(tuple.getFirst().getPosition().equals(fire.getPoint())){
						request = tuple.getFirst();
						range = tuple.getSecond();
						break;
					}
				}
				if(request == null){
					request = new ActionRequest(1, fire.getPoint(), new Extinguish(), communicationId);
					range = new Double(8); // starting range
				}
				range = range+4;
				this.communicationTool.setSendingRange(range);
				this.communicationTool.sendRequest(request);
				this.openRequests.put(request.getId(), new Pair<ActionRequest,Double>(request, range));
			}
		}
	}
	private String agentRequested(GridPoint p){
		for(Entry<String,ActionRequest> request: requestedAgents.entrySet()){
			if(request.getValue().getPosition().equals(p)){
				return request.getKey();
			}
		}
		return null;
	}

	@Override
	public void sendAnswers() {
		//answer inforeqeusts
		for(InformationRequest infoRequest : infoRequests)
		{
			GridPoint asked = infoRequest.getPosition();
			if(asked == null) //send my position
			{
				asked = getPosition();
			}
			Information info = this.belief.getInformation(asked, infoRequest.getInformationClass());
			if(info != null && RunEnvironment.getInstance().getCurrentSchedule().getTickCount() - info.getTimestamp() < 20){ // only "new" information
				communicationTool.sendInformation(info, infoRequest.getSenderID());
			}
		}
		infoRequests.clear();
		
		//Send offer
		//choose request with smallest distance (for now)
		//TODO Integrate importance: if distance difference insignificant (threshold), choose by importance
		GridPoint location = grid.getLocation(this);
		GridPoint intentionPosition = currentIntention.getPosition();
		double smallestDistance = Double.MAX_VALUE;
		//calculate distance to current intention (if patrol infinity)
		if(intentionPosition != null)
		{
			smallestDistance = grid.getDistance(intentionPosition, location);
		}
		ActionRequest chosenRequest = null;
		for(ActionRequest actionRequest : actionRequests.values())
		{
			GridPoint target = actionRequest.getPosition();
			double distance = grid.getDistance(location, target);
			if(smallestDistance > distance || target.equals(currentIntention.getPosition()))
			{
				smallestDistance = distance;
				chosenRequest = actionRequest;
			}
			//integrate into beliefs
			FireInformation fi = new FireInformation(target, 1);
			belief.addInformation(fi);
		}
		if(chosenRequest != null)
		{
			RequestOffer requestOffer = new RequestOffer(communicationId, 
					chosenRequest.getId(), smallestDistance, false);
			myOffer= chosenRequest;
			actionRequests.remove(chosenRequest.getId());
			communicationTool.sendRequestOffer(chosenRequest.getSenderID(), requestOffer);
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
			Pair<ActionRequest,Double> tuple = openRequests.get(offer.getRequestID());
			Request request = null;
			if(tuple!=null)
				request = tuple.getFirst();
			if(request!=null && agentRequested(request.getPosition())==null && (best == null || offer.getDistance()<best.getDistance())){
				bestOffers.put(request.getId(), offer);
			}
		}
		//send confirmations to best offers and add to requested agents
		for(Entry<Integer, RequestOffer> entry: bestOffers.entrySet()){
			communicationTool.sendRequestConfirm(entry.getValue().getSenderId(), new RequestConfirm(communicationId, entry.getValue().getRequestID()));
			ActionRequest confirmed = openRequests.get(entry.getKey()).getFirst();
			requestedAgents.put(entry.getValue().getSenderId(), confirmed);
			openRequests.remove(confirmed.getId());
		}
		//delete other offers
		offers.clear();
	}

	@Override
	public void doActions()
	{
		if (!flee()) 
		{
			// TODO act with respect to your intention (move, extinguish, wetline, woodcutting, check weather)
			
			// Check confirmation
			if(requestConfirmation != null)
			{
				//offer was for current intention
				if(myOffer.getPosition().equals(currentIntention.getPosition()))
				{
					currentIntention.addRequester(requestConfirmation.getSenderID(), requestConfirmation.getRequestID());
				}
				//change intention and send dismiss to all waiting agents
				else
				{
					changeIntention(new Intention(myOffer.getAction(), myOffer.getPosition(), requestConfirmation.getSenderID(),myOffer.getId()));
				}
				requestConfirmation = null;
			}
			myOffer = null;
			NdPoint me = getExactPosition();
			//execute intention
			boolean executeSuccess = currentIntention.getAction().
					execute(this, currentIntention.getPosition());
			if(!executeSuccess)
			{
				this.currentIntention = new Intention(new Patrol(), null, null, null);
			}
		}
	}

	
}
