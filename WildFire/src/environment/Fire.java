package environment;

import java.util.List;
import java.util.Random;

import agent.Information;
import agent.InformationProvider;
import agent.Statistic;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Fire implements InformationProvider {
	private double heat; //number of points to decrease woods lifepoints each iteration
	private Wind wind; //fire spreads in winds direction
	private Grid<Object> grid;	//actual cell in grid
	private Random random;
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	
	public Fire(double heat, Wind wind, Grid<Object> grid, Context<Object> context, ContinuousSpace<Object> space) {
		super();
		this.heat = heat;
		this.wind = wind;
		this.grid = grid;
		this.space = space;
		this.context = context;
		this.random = new Random();
		Statistic.getStatisticFromContext(context).incrementFireCount();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 996)
	public void step() {
		GridPoint pt = grid.getLocation(this);
		//propagation
		GridCellNgh<Wood> nghCreator = new GridCellNgh<Wood>(grid, pt,
				Wood.class, 1, 1);
		List<GridCell<Wood>> gridCells = nghCreator.getNeighborhood(true);
		double windX = Math.cos(this.wind.getWindDirection())*wind.getSpeed();
		double windY = Math.sin(this.wind.getWindDirection())*wind.getSpeed();
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
						if(random.nextDouble()<wind.getSpeed()*0.1){//TODO take length of windvector into consideration
							Fire f = new Fire(wind.getSpeed()*0.05*this.heat, this.wind, this.grid,this.context, this.space);
							context.add(f);
							grid.moveTo(f, cell.getPoint().getX(), cell.getPoint().getY());
							space.moveTo(f, cell.getPoint().getX(), cell.getPoint().getY());
						}
					}
					else{
						fire.increaseHeat(wind.getSpeed()*0.01*this.heat);//TODO take length of windvector into consideration
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
			double maxHeat =  material.burn(heat);
			this.heat+=(maxHeat-this.heat)*0.05*wind.getSpeed();
		}
	}
	
	public void increaseHeat(double add){
		this.heat+=add;
	}
	
	/**
	 * @param sub
	 * @return The amount of heat that was actually decreased.
	 */
	public double decreaseHeat(double sub, boolean byForester){
		this.heat-=sub;
		if(this.heat<=0){
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
			
			if(byForester)
			{
				Statistic.getStatisticFromContext(context).incrementExtinguishedFireCount();
			}
			
			return sub + this.heat; 
		}
		return sub;
	}
	
	@Override
	public FireInformation getInformation() {
		GridPoint location = grid.getLocation(this);
		return new FireInformation(location.getX(), location.getY(), heat);
	}
	
	public static class FireInformation extends Information {

		private double heat;
		
		private FireInformation(Integer positionX, Integer positionY, double heat) {
			super(positionX, positionY);
			this.heat = heat;
		}
		
		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public FireInformation(Integer positionX, Integer positionY)
		{
			super(positionX, positionY, true);
		}

		public double getHeat() {
			return heat;
		}
	}
}
