package wildFire;

import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Fire {
	private double heat; //number of points to decrease woods lifepoints each iteration
	private Wind wind; //fire spreads in winds direction
	private double maxHeat = 10;
	private Grid<Object> grid;	//actual cell in grid
	private Random random;
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	
	public Fire(double heat, double maxHeat, Wind wind, Grid<Object> grid, Context<Object> context, ContinuousSpace<Object> space) {
		super();
		this.heat = heat;
		this.wind = wind;
		this.grid = grid;
		this.space = space;
		this.context = context;
		this.random = new Random();
		this.maxHeat = maxHeat;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		GridPoint pt = grid.getLocation(this);
		//propagation
		GridCellNgh<Wood> nghCreator = new GridCellNgh<Wood>(grid, pt,
				Wood.class, 1, 1);
		List<GridCell<Wood>> gridCells = nghCreator.getNeighborhood(true);
		double windX = Math.cos(this.wind.getWindDirectionRadians())*wind.getSpeed();
		double windY = Math.sin(this.wind.getWindDirectionRadians())*wind.getSpeed();
		for (GridCell<Wood> cell : gridCells) {
			double xDiff = cell.getPoint().getX() - pt.getX();
			double yDiff = cell.getPoint().getY() - pt.getY();
			if(cell.items().iterator().hasNext() && windX*xDiff >= 0 && windY*yDiff>=0){//in wind direction
				Wood w = cell.items().iterator().next();
				//decrease wetness
				if(w.getWetness()>0){
					w.burn(wind.getSpeed()*heat); //TODO take length of windvector into consideration
				}
				//increase fire or spread fire
				else{
					Fire fire = null;
					for(Object o : grid.getObjectsAt(cell.getPoint().getX(), cell.getPoint().getY())){
						if(o instanceof Fire){
							fire = (Fire) o;
							break;
						}
					}
					if(fire == null){
						if(random.nextDouble()<wind.getSpeed()){//TODO take length of windvector into consideration
							Fire f = new Fire(wind.getSpeed()*this.heat, this.maxHeat, this.wind, this.grid,this.context, this.space);
							context.add(f);
							grid.moveTo(f, cell.getPoint().getX(), cell.getPoint().getY());
							space.moveTo(f, cell.getPoint().getX(), cell.getPoint().getY());
						}
					}
					else{
						fire.increaseHeat(wind.getSpeed()*this.heat);//TODO take length of windvector into consideration
					}
				}
			}
		}
		//burn actual 
		Wood material = null;
		for(Object o:grid.getObjectsAt(pt.getX(),pt.getY())){
			if(o instanceof Wood){
				material = (Wood)o;
				break;
			}
		}
		//no material -> fire dies
		if(material==null){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
		}
		else{
			material.burn(heat);
		}
		
	}
	public void increaseHeat(double add){
		this.heat+=add;
		if(this.heat>maxHeat)
			this.heat = maxHeat;
	}
	public void decreaseHeat(double sub){
		this.heat-=sub;
		if(this.heat<=0){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
		}
	}
	
}
