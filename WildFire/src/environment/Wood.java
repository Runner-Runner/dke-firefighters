package environment;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

public class Wood {
	private double wetness;	//increases by rain decreases by time and fire in neighborhood fire cannot enter if > 0 (threshold)
	private double power;	//life points - depends on material 
	private double material; //defines material factor, how much water can be stored, how fast it transpires and how hot this material can burn
	
	
	public Wood(double power, double material) {
		super();
		this.wetness = power*material;
		this.power = power;
		this.material = material;
	}
	
	public double burn(double decrease){
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
			return 0;
		}
		return this.material*this.power*0.1+this.material;
	}
	
	public double getWetness() {
		return wetness;
	}

	public void shower(double increase){
		this.wetness+=increase;
		if(this.wetness>this.power*material)
			this.wetness = this.power*material;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void transpire() {
		this.wetness-=this.wetness*material*0.1;
	}
	
}
