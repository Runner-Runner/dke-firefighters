package environment;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.GridPoint;

public class ForestFactory
{
	private double maxPower;
	private double minPower;
	private double maxMaterial;
	private double minMaterial;

	public ForestFactory(double maxPower, double minPower, double maxMaterial,
			double minMaterial)
	{
		super();
		this.maxPower = maxPower;
		this.minPower = minPower;
		this.maxMaterial = maxMaterial;
		this.minMaterial = minMaterial;
	}

	public Wood getWood(int x, int y)
	{
		double power = minPower + (maxPower - minPower)
				* RandomHelper.nextDouble();
		double material = minMaterial + (maxMaterial - minMaterial)
				* RandomHelper.nextDouble();
		return new Wood(power, material, new GridPoint(x, y));
	}

}
