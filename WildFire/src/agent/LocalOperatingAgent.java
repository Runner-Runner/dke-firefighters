package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import main.Pair;
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
import environment.Fire;
import environment.Fire.FireInformation;

/**
 * Implements an forester agent communicating in a range based on the urgency of the request, but incorporates 
 * mostly local information into the decision how to act.
 */
public class LocalOperatingAgent extends ForesterAgent
{
	public LocalOperatingAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate)
	{
		super(space, grid, speed, extinguishRate);
	}

	@Override
	public void checkNeighbourhood()
	{
		updateNeighborhoodBelief();

		// check if actual intention is obsolete
		if (currentIntention.getAction() instanceof Extinguish)
		{
			FireInformation fi = belief.getInformation(
					currentIntention.getPosition(), FireInformation.class);
			if (fi == null || fi.isEmptyInstance())
			{
				changeIntention(new Intention(new Patrol(), null, null, null));
			}
		}
		int startX = getPosition().getX();
		int startY = getPosition().getY();

		GridPoint nextFire = null;
		double closestDistance = Double.MAX_VALUE;
		double coldest = Double.MAX_VALUE;

		// Look for fire in direct neighborhood and choose coldest
		for (int offsetX = -SEEING_RANGE; offsetX <= SEEING_RANGE; offsetX++)
		{
			for (int offsetY = -SEEING_RANGE; offsetY <= SEEING_RANGE; offsetY++)
			{
				FireInformation fireInformation = belief.getInformation(
						new GridPoint(startX + offsetX, startY + offsetY),
						FireInformation.class);
				if (fireInformation != null
						&& !fireInformation.isEmptyInstance())
				{
					int discreteDistance = Math.max(Math.abs(offsetX),
							Math.abs(offsetY));
					double heat = fireInformation.getHeat();
					if (discreteDistance < closestDistance
							|| (discreteDistance == closestDistance && heat < coldest))
					{
						nextFire = fireInformation.getPosition();
						closestDistance = discreteDistance;
						coldest = heat;
					}
				}
			}
		}
		// change intention if better
		if (nextFire != null
				&& (currentIntention.getPosition() == null || grid.getDistance(
						currentIntention.getPosition(), getPosition()) > grid
						.getDistance(getPosition(), nextFire)))
		{
			changeIntention(new Intention(new Extinguish(), nextFire, null,
					null));
		}
		// clean open requests
		ArrayList<Integer> obsolete = new ArrayList<>();
		for(Entry<Integer,Pair<ActionRequest,Double>> entry:openRequests.entrySet())
		{
			GridPoint target = entry.getValue().getFirst().getPosition();
			FireInformation fi = belief.getInformation(target, FireInformation.class);
			if(fi.isEmptyInstance() || target.equals(currentIntention.getPosition()))
			{
				obsolete.add(entry.getKey());
			}
		}
		openRequests.keySet().removeAll(obsolete);
		//inform requested agents about obsolete intentions
		ArrayList<String> rejected = new ArrayList<>();
		for(Entry<String,ActionRequest> entry: requestedAgents.entrySet())
		{
			GridPoint target = entry.getValue().getPosition();
			if(belief.getInformation(target, FireInformation.class).isEmptyInstance())
			{
				communicationTool.sendRequestDismiss(entry.getKey(), new RequestDismiss(entry.getValue().getId(), communicationId));
				rejected.add(entry.getKey());
			}
		}
		requestedAgents.keySet().removeAll(rejected);
	}

	@Override
	public void doRequests()
	{
		// check dismiss
		for (RequestDismiss rd : this.rejections)
		{
			// check if your intention is obsolete
			if (currentIntention.removeRequester(rd.getRequestID()))
			{
				// Remove belief
				if(currentIntention.getAction() instanceof Extinguish)
					belief.addInformation(new FireInformation(currentIntention.getPosition()));
				changeIntention(new Intention(new Patrol(), null, null, null));
			}
			// requested agent found better fire
			else
			{
				Request request = this.requestedAgents.get(rd.getSenderID());
				if (request != null && request.getId() == rd.getRequestID())
				{
					this.requestedAgents.remove(rd.getSenderID());
				}
			}
		}
		this.rejections.clear();

		// check if you need help
		GridPoint location = grid.getLocation(this);

		GridCellNgh<Fire> nghFire = new GridCellNgh<>(grid, location,
				Fire.class, 1, 1);
		List<GridCell<Fire>> fires = nghFire.getNeighborhood(true);

		for (GridCell<Fire> fire : fires)
		{
			if (fire.size() > 0)
			{
				// my intention
				if (fire.getPoint().equals(currentIntention.getPosition()))
				{
					continue;
				}
				// agents already requested
				if (agentRequested(fire.getPoint()) != null)
				{
					continue;
				}
				ActionRequest request = null;
				Double range = null;
				// already sent request -> increase range
				for (Pair<ActionRequest, Double> tuple : openRequests.values())
				{
					if (tuple.getFirst().getPosition().equals(fire.getPoint()))
					{
						request = tuple.getFirst();
						range = tuple.getSecond();
						break;
					}
				}
				if (request == null)
				{
					request = new ActionRequest(1, fire.getPoint(),
							new Extinguish(), communicationId);
					range = new Double(8); // starting range
				}
				range = range + 4;
				this.communicationTool.setSendingRange(range);
				this.communicationTool.sendRequest(request);
				this.openRequests.put(request.getId(),
						new Pair<ActionRequest, Double>(request, range));
			}
		}
	}

	@Override
	public void sendAnswers()
	{
		// answer info requests
		for (InformationRequest infoRequest : infoRequests)
		{
			GridPoint asked = infoRequest.getPosition();
			Information info;
			if (asked == null) // send my position
			{
				asked = getPosition();
				info = getInformation();
			}
			else
			{
			info = this.belief.getInformation(asked,
					infoRequest.getInformationClass());
			}
			if (info != null
					&& RunEnvironment.getInstance().getCurrentSchedule()
							.getTickCount()
							- info.getTimestamp() < 20)
			{ // only "new" information
				communicationTool.sendInformation(info,
						infoRequest.getSenderID());
			}
		}
		infoRequests.clear();

		// Send offer
		// choose request with smallest distance (for now)
		GridPoint location = grid.getLocation(this);
		GridPoint intentionPosition = currentIntention.getPosition();
		double smallestDistance = Double.MAX_VALUE;
		// calculate distance to current intention (if patrol infinity)
		if (intentionPosition != null)
		{
			smallestDistance = grid.getDistance(intentionPosition, location);
		}
		ActionRequest chosenRequest = null;
		for (ActionRequest actionRequest : actionRequests.values())
		{
			GridPoint target = actionRequest.getPosition();
			double distance = grid.getDistance(location, target);
			if (smallestDistance > distance
					|| target.equals(currentIntention.getPosition()))
			{
				smallestDistance = distance;
				chosenRequest = actionRequest;
			}
			// integrate into beliefs
			FireInformation fi = new FireInformation(target, 1);
			belief.addInformation(fi);
		}
		if (chosenRequest != null)
		{
			RequestOffer requestOffer = new RequestOffer(communicationId,
					chosenRequest.getId(), smallestDistance, false);
			myOffer = chosenRequest;
			actionRequests.remove(chosenRequest.getId());
			communicationTool.sendRequestOffer(chosenRequest.getSenderID(),
					requestOffer);
		}
		// delete old requests
		LinkedList<Integer> old = new LinkedList<Integer>();
		double currentTimestamp = RunEnvironment.getInstance()
				.getCurrentSchedule().getTickCount();
		for (Request request : actionRequests.values())
		{
			if (currentTimestamp - request.getTimestamp() > 3)
			{
				old.add(request.getId());
			}
		}
		actionRequests.keySet().removeAll(old);
	}

	@Override
	public void checkResponses()
	{
		// check information answers
		for (Information i : messages)
		{
			this.belief.addInformation(i);
		}
		messages.clear();

		// choose best offers for each openRequest
		HashMap<Integer, RequestOffer> bestOffers = new HashMap<Integer, RequestOffer>();
		for (RequestOffer offer : this.offers)
		{
			RequestOffer best = bestOffers.get(offer.getRequestID());
			Pair<ActionRequest, Double> tuple = openRequests.get(offer
					.getRequestID());
			Request request = null;
			if (tuple != null)
				request = tuple.getFirst();
			if (request != null
					&& agentRequested(request.getPosition()) == null
					&& (best == null || offer.getDistance() < best
							.getDistance()))
			{
				bestOffers.put(request.getId(), offer);
			}
		}
		// send confirmations to best offers and add to requested agents
		for (Entry<Integer, RequestOffer> entry : bestOffers.entrySet())
		{
			communicationTool.sendRequestConfirm(
					entry.getValue().getSenderId(), new RequestConfirm(
							communicationId, entry.getValue().getRequestID()));
			ActionRequest confirmed = openRequests.get(entry.getKey())
					.getFirst();
			requestedAgents.put(entry.getValue().getSenderId(), confirmed);
			openRequests.remove(confirmed.getId());
		}
		// delete other offers
		offers.clear();
	}

	@Override
	public void doActions()
	{
		if (!flee())
		{
			// Check confirmation
			if (requestConfirmation != null)
			{
				// offer was for current intention
				if (myOffer.getPosition()
						.equals(currentIntention.getPosition()))
				{
					currentIntention.addRequester(
							requestConfirmation.getSenderID(),
							requestConfirmation.getRequestID());
				}
				// change intention and send dismiss to all waiting agents
				else
				{
					changeIntention(new Intention(myOffer.getAction(),
							myOffer.getPosition(),
							requestConfirmation.getSenderID(), myOffer.getId()));
				}
				requestConfirmation = null;
			}
			myOffer = null;
			// execute intention
			boolean executeSuccess = currentIntention.getAction().execute(this,
					currentIntention.getPosition());
			if (!executeSuccess)
			{
				//patrol in fire-direction
				double x = currentIntention.getPosition().getX()-getPosition().getX();
				double y = currentIntention.getPosition().getY()-getPosition().getY();
				this.currentIntention = new Intention(new Patrol(x,y), null, null,
						null);
			}
		}
	}

}
