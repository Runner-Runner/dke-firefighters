package agent.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import main.SimulationManager;
import agent.ForesterAgent;
import agent.ForesterAgent.AgentInformation;
import agent.communication.info.Information;
import agent.communication.request.Request;
import agent.communication.request.RequestConfirm;
import agent.communication.request.RequestDismiss;
import agent.communication.request.RequestOffer;
import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;

/**
 * Provides a tool for forester agents to communicate with each other and send
 * messages or requests to a single other agent or to all agents within a
 * specified sending range.
 */
public class CommunicationTool
{
	private ForesterAgent sender;
	private Grid<Object> grid;

	/**
	 * Local multicast sending range in grid tiles (in each cardinal direction).
	 * Null means send to all other agents (global broadcast).
	 */
	private Double sendingRange;

	public CommunicationTool(ForesterAgent sender, Grid<Object> grid)
	{
		this.sender = sender;
		this.grid = grid;
	}

	public void setSendingRange(double sendingRange)
	{
		this.sendingRange = sendingRange;
	}

	/**
	 * Send to all agents within the set sending range.
	 * 
	 * @param information
	 */
	public void sendInformation(Information information)
	{
		sendInformation(getAgentsInRange(sender, sendingRange), information);
	}

	/**
	 * Send to one agent.
	 * 
	 * @param information
	 * @param id ID of the recipient.
	 * @return
	 */
	public boolean sendInformation(Information information, String id)
	{
		return sendInformation(information, getAgent(id));
	}
	
	public void sendInformation(Information information,
			List<String> recipientIDs)
	{
		List<ForesterAgent> list = getAgents(recipientIDs);
		sendInformation(list, information);
	}

	public void sendRequest(Request request)
	{
		sendRequest(request, getAgentsInRange(sender, sendingRange));
	}

	public void sendRequest(List<String> recipientIDs, Request request)
	{
		sendRequest(request, getAgents(recipientIDs));
	}

	public boolean sendRequest(Request request, String id)
	{
		return sendRequest(request, getAgent(id));
	}

	/**
	 * Send request offer at request sender.
	 * 
	 * @param id
	 * @param ro
	 * @return Whether the recipient actually received the offer.
	 */
	public boolean sendRequestOffer(String id, RequestOffer ro)
	{
		ForesterAgent recipient = getAgent(id);

		if (recipient == null)
		{
			// recipient not reachable, possibly dead
			return false;
		} else
		{
			recipient.receiveOffer(ro);
			sender.addCosts(calculateDistance(sender, recipient));
			return true;
		}
	}

	/**
	 * Send request dismiss at request sender.
	 * 
	 * @param id
	 * @param dismiss
	 * @return Whether the recipient actually received the dismiss.
	 */
	public boolean sendRequestDismiss(String id, RequestDismiss dismiss)
	{
		ForesterAgent recipient = getAgent(id);

		if (recipient == null)
		{
			// recipient not reachable, possibly dead
			return false;
		} else
		{
			recipient.receiveDismiss(dismiss);
			sender.addCosts(calculateDistance(sender, recipient));
			return true;
		}
	}

	/**
	 * Send request confirmation at request sender.
	 * 
	 * @param id
	 * @param rc
	 * @return Whether the recipient actually received the confirmation.
	 */
	public boolean sendRequestConfirm(String id, RequestConfirm rc)
	{
		ForesterAgent recipient = getAgent(id);

		if (recipient == null)
		{
			// recipient not reachable, possibly dead
			return false;
		} else
		{
			recipient.receiveConfirmation(rc);
			sender.addCosts(calculateDistance(sender, recipient));
			return true;
		}
	}

	private void sendInformation(List<ForesterAgent> recipients,
			Information information)
	{
		for (ForesterAgent recipient : recipients)
		{
			sendInformation(information, recipient);
		}
	}

	private boolean sendInformation(Information information,
			ForesterAgent recipient)
	{
		if (recipient == null)
		{
			// recipient not reachable, possibly dead
			return false;
		} else
		{
			recipient.receiveInformation(information);
			sender.addCosts(calculateDistance(sender, recipient));
			return true;
		}
	}

	private void sendRequest(Request request, List<ForesterAgent> recipients)
	{
		for (ForesterAgent recipient : recipients)
		{
			sendRequest(request, recipient);
		}
	}

	private boolean sendRequest(Request request, ForesterAgent recipient)
	{
		if (recipient == null)
		{
			// recipient not reachable, possibly dead
			return false;
		} else
		{
			recipient.receiveRequest(request);
			sender.addCosts(calculateDistance(sender, recipient));
			return true;
		}
	}

	private List<ForesterAgent> getAgents(List<String> ids)
	{
		ArrayList<ForesterAgent> list = new ArrayList<ForesterAgent>(ids.size());
		Context<Object> context = SimulationManager.getContext();
		IndexedIterable<Object> objects = context
				.getObjects(ForesterAgent.class);
		for (Object obj : objects)
		{
			if (obj instanceof ForesterAgent)
			{
				ForesterAgent agent = (ForesterAgent) obj;
				if (ids.contains(agent.getCommunicationId()))
					list.add(agent);
			}
		}
		return list;
	}

	private ForesterAgent getAgent(String id)
	{
		Context<Object> context = SimulationManager.getContext();
		IndexedIterable<Object> objects = context
				.getObjects(ForesterAgent.class);

		for (Object obj : objects)
		{
			if (obj instanceof ForesterAgent)
			{
				ForesterAgent agent = (ForesterAgent) obj;
				if (agent.getCommunicationId().equals(id))
				{
					return agent;
				}
			}
		}
		return null;
	}

	public double calculateDistance(ForesterAgent a, ForesterAgent b)
	{
		GridPoint aP = grid.getLocation(a);
		GridPoint bP = grid.getLocation(b);
		return SimulationManager.calculateDistance(aP, bP);
	}

	/**
	 * @param originAgent
	 * @param sendingRange
	 * @return All agents in sending range, with the sender as origin.
	 */
	private List<ForesterAgent> getAgentsInRange(ForesterAgent originAgent,
			Double sendingRange)
	{
		List<ForesterAgent> recipients = new ArrayList<>();

		Context<Object> context = SimulationManager.getContext();
		IndexedIterable<Object> objects = context
				.getObjects(ForesterAgent.class);

		for (Object obj : objects)
		{
			if (obj instanceof ForesterAgent)
			{
				if (obj.equals(this.sender))
				{
					continue;
				}
				ForesterAgent agent = (ForesterAgent) obj;
				if (sendingRange == null
						|| SimulationManager.calculateDistance(
								originAgent.getPosition(), agent.getPosition()) <= sendingRange)
				{
					recipients.add(agent);
				}
			}
		}
		return recipients;
	}

	/**
	 * @param originAgent
	 * @param sendingRange
	 * @return All agent information in sending range, with the sender as origin.
	 */
	public static List<AgentInformation> getAgentInformationInRange(
			ForesterAgent originAgent, Integer sendingRange)
	{
		List<AgentInformation> agentInformationList = new ArrayList<>();

		Collection<AgentInformation> allAgentInformation = originAgent
				.getBelief().getAllInformation(AgentInformation.class);
		for (AgentInformation agentInformation : allAgentInformation)
		{
			GridPoint gp = agentInformation.getPosition();
			if (sendingRange == null
					|| SimulationManager.calculateDistance(
							originAgent.getPosition(), gp) <= sendingRange)
			{
				agentInformationList.add(agentInformation);
			}
		}
		return agentInformationList;
	}
}
