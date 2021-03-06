package main;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import statistics.GraveyardStatistic;
import statistics.Statistic;
import agent.LocalOperatingAgent;
import environment.CloudFactory;
import environment.FireFactory;
import environment.ForestFactory;
import environment.Wind;
import environment.Wood;

/**
 * Sets up simulation environment and attributes.
 * 
 * @author Daniel
 *
 */
public class SimulationBuilder implements ContextBuilder<Object>
{
	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context<Object> context)
	{
		Parameters params = RunEnvironment.getInstance().getParameters();

		int forestDim = (Integer) params.getValue("forest_dim");

		double maxWindSpeed = (Double) params.getValue("max_wind_speed");
		double cloudRate = (Double) params.getValue("cloud_rate");
		double sparkingFactor = (Double) params.getValue("sparking_factor");
		int numberAgents = (Integer) params.getValue("number_agents");

		int maxCloudDim = 10;
		int minCloudDim = 3;
		double maxRain = 0.3;
		double minRain = 0.1;
		double maxCloudTank = 6000;
		double minCloudTank = 4000;

		double maxWoodHealth = 1500;
		double minWoodHealth = 800;
		double maxMaterialFactor = 0.2;
		double minMaterialFactor = 0.05;


		SimulationManager.setContext(context);

		// id of this context
		context.setId("WildFire");

		// create continuous space (floating point coordinates)
		RandomCartesianAdder<Object> terrainAdder = new RandomCartesianAdder<Object>(); // adder
																						// who
																						// places
																						// objects
																						// in
																						// terrain

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, terrainAdder,
				new repast.simphony.space.continuous.StrictBorders(),
				forestDim, forestDim);
		SimulationManager.setSpace(space);

		// create grid (to use for neighbourhood)
		SimpleGridAdder<Object> gridAdder = new SimpleGridAdder<Object>(); // adder
																			// who
																			// places
																			// objects
																			// in
																			// grid

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		// the boolean determines if more than one object can occupy a gridcell
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new StrictBorders(),
						gridAdder, true, forestDim, forestDim));
		SimulationManager.setGrid(grid);

		// -------------------------------------------------
		// ---------generate objects within the map --------------
		// -------------------------------------------------

		// wind (simulates global wind-speed and -direction)
		Wind wind = new Wind(maxWindSpeed);
		context.add(wind);

		// CloudFactory (creates clouds, which enter the map with respect to the
		// actual wind-direction)
		CloudFactory cf = new CloudFactory(context, wind, cloudRate,
				minCloudDim, maxCloudDim, forestDim, maxRain, minRain,
				maxCloudTank, minCloudTank);
		context.add(cf);

		// add forest
		// value for statistics
		double totalWoodHealth = 0;
		ForestFactory forest = new ForestFactory(maxWoodHealth, minWoodHealth,
				maxMaterialFactor, minMaterialFactor);
		for (int i = 0; i < forestDim; i++)
		{
			for (int j = 0; j < forestDim; j++)
			{
				Wood w = forest.getWood(i, j);
				totalWoodHealth += w.getHealth();
				context.add(w);
				space.moveTo(w, i + 0.5, j + 0.5);
			}
		}

		// create agents
		for (int i = 0; i < numberAgents; i++)
		{
			double speed = RandomHelper.nextDoubleFromTo(0.2, 0.5);
			double extinguish = RandomHelper.nextDoubleFromTo(0.15, 0.2);
			int x = RandomHelper.nextIntFromTo(0, forestDim - 1);
			int y = RandomHelper.nextIntFromTo(0, forestDim - 1);
			LocalOperatingAgent agent = new LocalOperatingAgent(space, grid, speed, extinguish);
			context.add(agent);
			space.moveTo(agent, x, y);
		}

		FireFactory fire = new FireFactory(context, grid, space, wind,
				sparkingFactor, forestDim);
		context.add(fire);

		// -------------------------------------

		// place the objects in the grid corresponding their continuous position
		for (Object o : context)
		{
			NdPoint pt = space.getLocation(o);
			grid.moveTo(o, (int) pt.getX(), (int) pt.getY());
		}

		context.add(GraveyardStatistic.getInstance());

		Statistic statistic = Statistic.getInstance();
		statistic.reset();
		statistic.setGridSize(grid.getDimensions().getWidth(), grid
				.getDimensions().getHeight());
		statistic.setTotalWoodHealth(totalWoodHealth);
		statistic.setTotalAgentCount(numberAgents);
		context.add(statistic);

		RunEnvironment.getInstance().endAt(30000);
		return context;
	}

}
