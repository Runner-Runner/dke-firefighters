package main;

import environment.CloudFactory;
import environment.FireFactory;
import environment.ForestFactory;
import environment.Wind;
import environment.Wood;
import agent.SimpleAgent;
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

public class SimulationBuilder implements ContextBuilder<Object>{
	

	
	
	@Override
	public Context build(Context<Object> context) {
		Parameters  params = RunEnvironment.getInstance (). getParameters ();
		
		int forestDim = (Integer)params.getValue("forest_dim");
		
		double maxWindSpeed = (Double)params.getValue("max_wind_speed");
		int maxCloudDim = (Integer)params.getValue("max_cloud_dim");
		int minCloudDim = (Integer)params.getValue("min_cloud_dim");
		double cloudRate = (Double)params.getValue("cloud_rate");
		double maxWoodHealth = (Double)params.getValue("max_wood_health");
		double minWoodHealth = (Double)params.getValue("min_wood_health");
		double maxMaterialFactor = (Double)params.getValue("max_material_factor");
		double minMaterialFactor = (Double)params.getValue("min_material_factor");
		double sparkingFactor = (Double)params.getValue("sparking_factor");
		double maxCloudTank = (Double)params.getValue("max_cloud_tank");
		double minCloudTank = (Double)params.getValue("min_cloud_tank");
		double maxRain = (Double)params.getValue("max_rain");
		double minRain = (Double)params.getValue("min_rain");
		
		//id of this context
		context.setId("WildFire");
		
		//create continuous space (floating point coordinates)
		RandomCartesianAdder<Object> terrainAdder = new RandomCartesianAdder<Object>(); //adder who places objects in terrain
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, terrainAdder , new repast.simphony.space.continuous.StrictBorders(), forestDim+2*maxCloudDim, forestDim+2*maxCloudDim);
		
		//create grid (to use for neighbourhood)
		SimpleGridAdder<Object> gridAdder = new SimpleGridAdder<Object>(); //adder who places objects in grid
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		//the boolean determines if more than one object can occupy a gridcell
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters <Object >(new StrictBorders(), gridAdder, true, forestDim+2*maxCloudDim, forestDim+2*maxCloudDim));
		
		
		//-------------------------------------------------
		//---------generate objects within the map --------------
		//-------------------------------------------------
		
		//wind (simulates global wind-speed and -direction)
		Wind wind = new Wind(maxWindSpeed);
		context.add(wind);
		
		//CloudFactory (creates clouds, which enter the map with respect to the actual wind-direction)
		CloudFactory cf = new CloudFactory(context, wind, space, grid, cloudRate, minCloudDim, maxCloudDim, forestDim, maxRain, minRain, maxCloudTank, minCloudTank);
		context.add(cf);
		
		//add forrest
		ForestFactory forrest = new ForestFactory(maxWoodHealth, minWoodHealth, maxMaterialFactor, minMaterialFactor);
		for(int i = maxCloudDim;i<maxCloudDim+forestDim;i++){
			for(int j = maxCloudDim;j<maxCloudDim+forestDim;j++){
				Wood w = forrest.getWood();
				context.add(w);
				space.moveTo(w, i, j);
			}
		}
		
		//create agents
		for(int i = 0;i<10;i++){
			context.add(new SimpleAgent(space, grid, 1, 2));
		}
		
		FireFactory fire = new FireFactory(context, grid, space, wind, sparkingFactor, forestDim, maxCloudDim);
		context.add(fire);
		
		//-------------------------------------
		
		//place the objects in the grid corresponding their continuous position
		for(Object o: context){
			NdPoint pt = space.getLocation(o);
			grid.moveTo(o, (int)pt.getX(), (int)pt.getY());
		}
		
		
		
		return context;
	}
	
}
