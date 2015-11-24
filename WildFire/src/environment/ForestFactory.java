package environment;

import java.util.Random;

import repast.simphony.space.grid.GridPoint;

public class ForestFactory {
	private double maxPower;
	private double minPower;
	private Random random;
	private double maxMaterial;
	private double minMaterial;
	public ForestFactory(double maxPower, double minPower, double maxMaterial,
			double minMaterial) {
		super();
		this.maxPower = maxPower;
		this.minPower = minPower;
		this.maxMaterial = maxMaterial;
		this.minMaterial = minMaterial;
		this.random = new Random();
	}
	
	public Wood getWood(int x, int y){
		double power = minPower+(maxPower-minPower)*random.nextDouble();
		double material = minMaterial+(maxMaterial-minMaterial)*random.nextDouble();
		return new Wood(power, material, new GridPoint(x,y));
	}
	
}
