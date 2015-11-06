package wildFire;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

public class Wood {
	private double wetness;	//increases by rain decreases by time and fire in neighbourhood fire cannot enter if > 0 (threshold)
	private double defaultWetness; //there is a default wetness - cannot decrease by time lower than this value
	private double power;	//life points - depends on material 
	private double maxWetness; //maximum wetness
	private double transpire; //wetness decreases over time
	
	
	public Wood(double power, double defaultWetness, double maxWetness, double transpire) {
		super();
		this.wetness = defaultWetness;
		this.defaultWetness = defaultWetness;
		this.power = power;
		this.transpire = transpire;
	}
	
	public boolean burn(double decrease){
		if(this.wetness>0){
			this.wetness-=decrease;
			if(this.wetness<0){
				this.power-=this.wetness;
				this.wetness = 0;
			}
		}
		else{
			this.power -= decrease;
		}
		if(this.power <= 0){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
			return false;
		}
		return true;
	}
	
	public double getWetness() {
		return wetness;
	}

	public void shower(double increase){
		this.wetness+=increase;
		if(this.wetness>this.maxWetness)
			this.wetness = this.maxWetness;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void transpire() {
		if(this.wetness>this.defaultWetness){
			this.wetness-=transpire;
		}
	}
	
}
