package environment;

import java.util.Iterator;
import java.util.Random;

import main.CommonKnowledge;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class FireFactory 
{
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	private Context<Object> context;
	private double frequency;
	private Wind wind;
	private Random random;
	private int forestDim;
	private int maxCloudDim;
	
	public FireFactory(Context<Object> context, Grid<Object> grid, ContinuousSpace<Object> space, Wind wind,
			double frequency, int forestDim, int maxCloudDim) {
		super();
		this.context = context;
		this.grid = grid;
		this.wind = wind;
		this.space = space;
		this.frequency = frequency;
		this.random = new Random();
		this.forestDim = forestDim;
		this.maxCloudDim = maxCloudDim;
	}
	
	
	@ScheduledMethod(start = 1, interval = CommonKnowledge.GENERAL_SCHEDULE_TICK_RATE*CommonKnowledge.FIRE_FACTOR, priority = 997)
	public void step() {
		int x = maxCloudDim+ random.nextInt(forestDim);
		int y = maxCloudDim + random.nextInt(forestDim);
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
				double heat = 2;
				Fire newFire = new Fire(heat, wind);
				context.add(newFire);
				grid.moveTo(newFire, x,y);
				space.moveTo(newFire, x,y);
			}
		}
	}
	
}
