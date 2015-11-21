package environment;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

public class Wood {
	private double wetness;	//increases by rain decreases by time and fire in neighborhood fire cannot enter if > 0 (threshold)
	private double health;	//life points - depends on material 
	private double material; //defines material factor, how much water can be stored, how fast it transpires and how hot this material can burn
	
	
	public Wood(double health, double material) {
		super();
		this.wetness = health*material;
		this.health = health;
		this.material = material;
	}
	
	public double burn(double decrease){
		if(this.wetness>0){
			this.wetness-=decrease;
			if(this.wetness<0){
				this.health += this.wetness;
				this.wetness = 0;
			}
		}
		else{
			this.health -= decrease;
		}
		if(this.health <= 0){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
			return 0;
		}
		return this.material*this.health*0.1+this.material;
	}
	
	public double getWetness() {
		return wetness;
	}
	public double getHealth(){
		return this.health;
	}
	public void shower(double increase){
		this.wetness+=increase;
		if(this.wetness>this.health*material)
			this.wetness = this.health*material;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 993)
	public void transpire() {
		this.wetness-=this.wetness*material*0.1;
	}
	
}
