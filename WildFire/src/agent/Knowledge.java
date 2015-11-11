package agent;

import environment.Cloud.CloudInformation;
import environment.Wind.WindInformation;

public class Knowledge {
	private WindInformation windInformation;
	private InformationMap<CloudInformation> cloudInformationMap;

	public Knowledge() {
		cloudInformationMap = new InformationMap<>();
	}

	public void setWindInformation(WindInformation windInformation)
	{
		this.windInformation = windInformation;
	}

	public WindInformation getWindInformation()
	{
		return windInformation;
	}
	
	public InformationMap<CloudInformation> getCloudInformationMap() {
		return cloudInformationMap;
	}
}
