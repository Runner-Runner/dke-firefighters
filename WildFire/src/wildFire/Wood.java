package wildFire;

public class Wood {
	private double wetness;	//increases by rain decreases by time and fire in neighbourhood fire cannot enter if > 0 (threshold)
	private double defaultWetness; //there is a default wetness - cannot decrease by time lower than this value
	private double power;	//life points - depends on material 
}
