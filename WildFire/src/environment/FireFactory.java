package environment;

import java.util.Iterator;
import java.util.Random;

import main.SimulationBuilder;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class FireFactory {
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	private Context<Object> context;
	private double frequency;
	private Wind wind;
	private Random random;
	
	public FireFactory(Context<Object> context, Grid<Object> grid, ContinuousSpace<Object> space, Wind wind,
			double frequency) {
		super();
		this.context = context;
		this.grid = grid;
		this.wind = wind;
		this.space = space;
		this.frequency = frequency;
		this.random = new Random();
	}
	
	
	@ScheduledMethod(start = 1, interval = 5)
	public void step() {
		int x = SimulationBuilder.MAX_CLOUD_DIM + random.nextInt(SimulationBuilder.FOREST_DIM);
		int y = SimulationBuilder.MAX_CLOUD_DIM + random.nextInt(SimulationBuilder.FOREST_DIM);
		Iterator<Object> it = grid.getObjectsAt(x,y).iterator();
		Wood w = null;
		Fire f = null;
		while(it.hasNext()){
			Object o = it.next();
			if(o instanceof Fire){
				f = (Fire)o;
				break;
			}
			else if(o instanceof Wood){
				w = (Wood)o;
			}
		}
		// wood without fire
		if(w != null && f == null){
			//TODO with respect to wetness and material
			if(random.nextDouble()<frequency){
				Fire newFire = new Fire(2, wind, grid, context, space);
				context.add(newFire);
				grid.moveTo(newFire, x,y);
				space.moveTo(newFire, x,y);
			}
		}
	}
	
}
