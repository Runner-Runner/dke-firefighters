package agent.communication.info;

/**
 * Implementing classes provide an information object that serves as a snapshot
 * of their attributes at a given time step.
 */
public interface InformationProvider
{
	public Information getInformation();
}
