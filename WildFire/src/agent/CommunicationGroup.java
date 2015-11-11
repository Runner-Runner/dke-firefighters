package agent;

import java.util.ArrayList;
import java.util.List;

import environment.Wind.WindInformation;

public class CommunicationGroup {
	
	private List<ForesterAgent> groupMembers;
	
	public CommunicationGroup()
	{
		groupMembers = new ArrayList<>();
	}
	
	public void sendWindInformation(WindInformation windInformation)
	{
//		for()
	}
}
