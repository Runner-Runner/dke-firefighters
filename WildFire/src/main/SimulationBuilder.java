package main;

import java.util.Random;

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
import agent.BDIAgent;
import environment.CloudFactory;
import environment.Fire;
import environment.FireFactory;
import environment.ForestFactory;
import environment.Wind;
import environment.Wood;

public class SimulationBuilder implements ContextBuilder<Object>{
	
	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context<Object> context) {
		Parameters  params = RunEnvironment.getInstance (). getParameters ();
		
		int forestDim = (Integer)params.getValue("forest_dim");
		
		double maxWindSpeed = (Double)params.getValue("max_wind_speed");
		
		int maxCloudDim = (Integer)params.getValue("max_cloud_dim");
		int minCloudDim = (Integer)params.getValue("min_cloud_dim");
		double maxRain = (Double)params.getValue("max_rain");
		double minRain = (Double)params.getValue("min_rain");
		double maxCloudTank = (Double)params.getValue("max_cloud_tank");
		double minCloudTank = (Double)params.getValue("min_cloud_tank");
		double cloudRate = (Double)params.getValue("cloud_rate");

		double maxWoodHealth = (Double)params.getValue("max_wood_health");
		double minWoodHealth = (Double)params.getValue("min_wood_health");
		double maxMaterialFactor = (Double)params.getValue("max_material_factor");
		double minMaterialFactor = (Double)params.getValue("min_material_factor");
		
		double sparkingFactor = (Double)params.getValue("sparking_factor");
		
		int numberAgents = (Integer)params.getValue("number_agents");
		
		CommonKnowledge.setContext(context);
		
		//id of this context
		context.setId("WildFire");
		
		//create continuous space (floating point coordinates)
		RandomCartesianAdder<Object> terrainAdder = new RandomCartesianAdder<Object>(); //adder who places objects in terrain
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, terrainAdder , new repast.simphony.space.continuous.StrictBorders(), forestDim+2*maxCloudDim, forestDim+2*maxCloudDim);
		CommonKnowledge.setSpace(space);
		
		//create grid (to use for neighbourhood)
		SimpleGridAdder<Object> gridAdder = new SimpleGridAdder<Object>(); //adder who places objects in grid
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		//the boolean determines if more than one object can occupy a gridcell
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters <Object >(new StrictBorders(), gridAdder, true, forestDim+2*maxCloudDim, forestDim+2*maxCloudDim));
		CommonKnowledge.setGrid(grid);
		
		//-------------------------------------------------
		//---------generate objects within the map --------------
		//-------------------------------------------------
		
		//wind (simulates global wind-speed and -direction)
		Wind wind = new Wind(maxWindSpeed);
		context.add(wind);
		
		//CloudFactory (creates clouds, which enter the map with respect to the actual wind-direction)
		CloudFactory cf = new CloudFactory(context, wind, cloudRate, minCloudDim, maxCloudDim, forestDim, maxRain, minRain, maxCloudTank, minCloudTank);
		context.add(cf);
		
		//add forest
		//value for statistics
		double totalWoodHealth = 0;
		ForestFactory forest = new ForestFactory(maxWoodHealth, minWoodHealth, maxMaterialFactor, minMaterialFactor);
		for(int i = maxCloudDim;i<maxCloudDim+forestDim;i++){
			for(int j = maxCloudDim;j<maxCloudDim+forestDim;j++){
				Wood w = forest.getWood(i, j);
				totalWoodHealth += w.getHealth();
				context.add(w);
				space.moveTo(w, i, j);
			}
		}
		
		//create agents
		for(int i = 0;i<numberAgents;i++){
			double speed = RandomHelper.nextDoubleFromTo(1, 2);
			Random r = new Random();
			int x = r.nextInt(forestDim)+maxCloudDim;
			int y = r.nextInt(forestDim)+maxCloudDim;
			BDIAgent agent = new BDIAgent(space, grid, speed, 2);
			context.add(agent);
			space.moveTo(agent, x,y);
		}
		
		FireFactory fire = new FireFactory(context, grid, space, wind, sparkingFactor, forestDim, maxCloudDim);
		context.add(fire);
		//Test
//		Fire fire = new Fire(1, wind, grid, context, space);
//		Fire fire2 = new Fire(1, wind, grid, context, space);
//		Fire fire3 = new Fire(1, wind, grid, context, space);
//		Fire fire4 = new Fire(1, wind, grid, context, space);
//		context.add(fire);
//		context.add(fire2);
//		context.add(fire3);
//		grid.moveTo(fire, 24, 23);
//		space.moveTo(fire, 24, 23);
//		grid.moveTo(fire2, 25, 23);
//		space.moveTo(fire2, 25, 23);
//		grid.moveTo(fire3, 24, 24);
//		space.moveTo(fire3, 24, 24);
		
		//-------------------------------------
		
		//place the objects in the grid corresponding their continuous position
		for(Object o: context){
			NdPoint pt = space.getLocation(o);
			grid.moveTo(o, (int)pt.getX(), (int)pt.getY());
		}

		context.add(GraveyardStatistic.getInstance());
		
		Statistic statistic = Statistic.getInstance();
		statistic.setGridSize(grid.getDimensions().getWidth(), grid.getDimensions().getHeight());
		statistic.setTotalWoodHealth(totalWoodHealth);
		statistic.setTotalAgentCount(numberAgents);
		context.add(statistic);
		
		return context;
	}
	
}
