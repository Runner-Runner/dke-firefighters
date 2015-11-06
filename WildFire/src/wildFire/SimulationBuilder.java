package wildFire;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;

public class SimulationBuilder implements ContextBuilder<Object>{
	public static final int FOREST_DIM = 20;
	
	public static final int MAX_CLOUD_DIM = 6;
	public static final int MIN_CLOUD_DIM = 3;
	public static final double CLOUD_FREQUENCY = 2;
	
	public static final double MAX_WIND_SPEED = 0.8;
	
	
	@Override
	public Context build(Context<Object> context) {
		//id of this context
		context.setId("WildFire");
		
		//create continuous space (floating point coordinates)
		RandomCartesianAdder<Object> terrainAdder = new RandomCartesianAdder<Object>(); //adder who places objects in terrain
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, terrainAdder , new repast.simphony.space.continuous.StrictBorders(), FOREST_DIM+2*MAX_CLOUD_DIM, FOREST_DIM+2*MAX_CLOUD_DIM);
		
		//create grid (to use for neighbourhood
		SimpleGridAdder<Object> gridAdder = new SimpleGridAdder<Object>(); //adder who places objects in grid
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		//the boolean determins, if more than one object can occupy a gridcell
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters <Object >(new StrictBorders(), gridAdder, true, FOREST_DIM+2*MAX_CLOUD_DIM, FOREST_DIM+2*MAX_CLOUD_DIM));
		
		
		//-------------------------------------------------
		//---------generate objects within the map --------------
		//-------------------------------------------------
		
		//wind (simulates global wind-speed and -direction)
		Wind wind = new Wind(MAX_WIND_SPEED);
		context.add(wind);
		
		//CloudFactory (creates clouds, which enter the map with respect to the actual wind-direction)
		CloudFactory cf = new CloudFactory(context, wind, space, grid, CLOUD_FREQUENCY, MIN_CLOUD_DIM, MAX_CLOUD_DIM);
		context.add(cf);
		
		//add forrest
		for(int i = MAX_CLOUD_DIM;i<MAX_CLOUD_DIM+FOREST_DIM;i++){
			for(int j = MAX_CLOUD_DIM;j<MAX_CLOUD_DIM+FOREST_DIM;j++){
				Wood w = new Wood(1000,50,100,1);
				context.add(w);
				space.moveTo(w, i, j);
			}
		}
		
		//create agents
		for(int i = 0;i<1;i++){
			context.add(new StupidAgent(space, grid));
		}
		
		Fire fire = new Fire(3,20,wind,grid,context,space);
		context.add(fire);
		space.moveTo(fire, 20,20);
		grid.moveTo(fire, 20,20);
		
		//-------------------------------------
		
		//place the objects in the grid corresponding their continuous position
		for(Object o: context){
			NdPoint pt = space.getLocation(o);
			grid.moveTo(o, (int)pt.getX(), (int)pt.getY());
		}
		
		
		
		return context;
	}
	
}
