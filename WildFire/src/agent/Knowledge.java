package agent;

import environment.Cloud.CloudInformation;
import environment.Fire.FireInformation;
import environment.Wind.WindInformation;

public class Knowledge {
	private WindInformation windInformation;
	private InformationMap<CloudInformation> cloudInformationMap;
	private InformationMap<FireInformation> fireInformationMap;

	public Knowledge() {
		cloudInformationMap = new InformationMap<>();
		fireInformationMap = new InformationMap<>();
	}

	public void setWindInformation(WindInformation windInformation)
	{
		if(windInformation.isNewerInformation(this.windInformation))
		{
			this.windInformation = windInformation;
		}
	}

	public WindInformation getWindInformation()
	{
		return windInformation;
	}
	
	public InformationMap<CloudInformation> getCloudInformationMap() {
		return cloudInformationMap;
	}
	
	public InformationMap<FireInformation> getFireInformationMap() {
		return fireInformationMap;
	}
}
