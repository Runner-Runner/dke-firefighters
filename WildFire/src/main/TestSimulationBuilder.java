package main;


import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
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
import environment.Fire;
import environment.ForestFactory;
import environment.Wind;
import environment.Wood;

public class TestSimulationBuilder implements ContextBuilder<Object>{
	
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
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, terrainAdder , new repast.simphony.space.continuous.StrictBorders(), forestDim+2, forestDim+2);
		CommonKnowledge.setSpace(space);
		
		//create grid (to use for neighbourhood)
		SimpleGridAdder<Object> gridAdder = new SimpleGridAdder<Object>(); //adder who places objects in grid
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		//the boolean determines if more than one object can occupy a gridcell
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters <Object >(new StrictBorders(), gridAdder, true, forestDim+2, forestDim+2));
		CommonKnowledge.setGrid(grid);
		
		//-------------------------------------------------
		//---------generate objects within the map --------------
		//-------------------------------------------------
		
		//wind (simulates global wind-speed and -direction)
		Wind wind = new Wind(0);
		context.add(wind);
		
		//CloudFactory (creates clouds, which enter the map with respect to the actual wind-direction)
//		CloudFactory cf = new CloudFactory(context, wind, cloudRate, minCloudDim, maxCloudDim, forestDim, maxRain, minRain, maxCloudTank, minCloudTank);
//		context.add(cf);
		
		//add forest
		//value for statistics
		double totalWoodHealth = 0;
		//live forever
		ForestFactory forest = new ForestFactory(100000000, 100000000, maxMaterialFactor, minMaterialFactor);
		for(int i = 1;i<=forestDim;i++){
			for(int j = 1;j<=forestDim;j++){
				Wood w = forest.getWood(i, j);
				totalWoodHealth += w.getHealth();
				context.add(w);
				space.moveTo(w, i, j);
			}
		}
		
		//create agents
//		for(int i = 0;i<numberAgents;i++){
//			double speed = RandomHelper.nextDoubleFromTo(0.2, 0.5);
//			double extinguish = RandomHelper.nextDoubleFromTo(0.3, 1.0);
//			int x = RandomHelper.nextIntFromTo(1,forestDim);
//			int y = RandomHelper.nextIntFromTo(1,forestDim);
//			BDIAgent agent = new BDIAgent(space, grid, speed, extinguish);
//			context.add(agent);
//			space.moveTo(agent, x,y);
//		}
		
		int[][] posArray =
			{
				{
					22, 22
				},
				{
					22, 23
				},
				{
					21, 23
				},
				{
					21, 22
				},
				{
					23, 22
				},	
				{
					23, 23
				},
				{
					22, 25
				},
				{
					23, 25
				},
			};
		
		for(int[] pos : posArray)
		{
			BDIAgent agent = new BDIAgent(space, grid, 0.3, 0);
			context.add(agent);
			space.moveTo(agent, pos[0], pos[1]);
		}
		
		//live forever
		Fire newFire = new Fire(1000, wind);
		context.add(newFire);
		grid.moveTo(newFire, 22, 24);
		space.moveTo(newFire, 22, 24);
		
//		FireFactory fire = new FireFactory(context, grid, space, wind, sparkingFactor, forestDim);
//		context.add(fire);
		
		//-------------------------------------
		
		//place the objects in the grid corresponding their continuous position
		for(Object o: context){
			NdPoint pt = space.getLocation(o);
			grid.moveTo(o, (int)pt.getX(), (int)pt.getY());
		}

		context.add(GraveyardStatistic.getInstance());
		
		Statistic statistic = Statistic.getInstance();
		statistic.reset();
		statistic.setGridSize(grid.getDimensions().getWidth(), grid.getDimensions().getHeight());
		statistic.setTotalWoodHealth(totalWoodHealth);
		statistic.setTotalAgentCount(numberAgents);
		context.add(statistic);
		
//		RunEnvironment.getInstance().endAt(4000);
		return context;
	}
	
}
