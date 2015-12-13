package environment;

import java.util.Iterator;

import main.SimulationManager;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
/**
 * This class is used to create fire-spots by random but with respect to wind-speed
 * 
 * @author carsten
 *
 */
public class FireFactory
{
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	private Context<Object> context;
	private double frequency;
	private Wind wind;
	private int forestDim;
	
	/**
	 * create a firefactory
	 * @param context
	 * @param grid
	 * @param space
	 * @param wind
	 * @param frequency chance to create a new fire
	 * @param forestDim
	 */
	public FireFactory(Context<Object> context, Grid<Object> grid,
			ContinuousSpace<Object> space, Wind wind, double frequency,
			int forestDim)
	{
		super();
		this.context = context;
		this.grid = grid;
		this.wind = wind;
		this.space = space;
		this.frequency = frequency;
		this.forestDim = forestDim;
	}

	@ScheduledMethod(start = 1, interval = SimulationManager.GENERAL_SCHEDULE_TICK_RATE
			* SimulationManager.FIRE_FACTOR, priority = 997)
	public void step()
	{
		//choose random cell
		int x = RandomHelper.nextIntFromTo(0, forestDim - 1);
		int y = RandomHelper.nextIntFromTo(0, forestDim - 1);
		Iterator<Object> it = grid.getObjectsAt(x, y).iterator();
		Wood w = null;
		Fire f = null;
		//check if there is already fire or wood in the chosen cell
		while (it.hasNext())
		{
			Object o = it.next();
			if (o instanceof Fire)
			{
				f = (Fire) o;
				break;
			} else if (o instanceof Wood)
			{
				w = (Wood) o;
			}
		}
		// wood without fire
		if (w != null && f == null)
		{
			// try to create a fire
			if (RandomHelper.nextDouble() < frequency)
			{
				double heat = 2;
				Fire newFire = new Fire(heat, wind);
				context.add(newFire);
				grid.moveTo(newFire, x, y);
				space.moveTo(newFire, x + 0.5, y + 0.5);
			}
		}
		
//		This section was used to print out the agents coverage. 
//		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()>10000.0){
//			PrintWriter writer;
//			try {
//				writer = new PrintWriter("covering.csv", "UTF-8");
//				long[][] covering = Statistic.getInstance().getCovering();
//				for(int i = 0;i<50;i++){
//					for(int j = 0;j<50;j++){
//						writer.print(covering[i][j]+";");
//					}
//					writer.println();
//				}
//				writer.close();
//			} catch (FileNotFoundException | UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		}
	}

}
