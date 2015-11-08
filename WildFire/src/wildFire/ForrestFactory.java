package wildFire;

import java.util.Random;

public class ForrestFactory {
	private double maxPower;
	private double minPower;
	private Random random;
	private double maxMaterial;
	private double minMaterial;
	public ForrestFactory(double maxPower, double minPower, double maxMaterial,
			double minMaterial) {
		super();
		this.maxPower = maxPower;
		this.minPower = minPower;
		this.maxMaterial = maxMaterial;
		this.minMaterial = minMaterial;
		this.random = new Random();
	}
	
	public Wood getWood(){
		double power = minPower+(maxPower-minPower)*random.nextDouble();
		double material = minMaterial+(maxMaterial-minMaterial)*random.nextDouble();
		return new Wood(power,material);
	}
	
}
