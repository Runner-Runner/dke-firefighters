package environment;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import agent.Information;
import agent.InformationProvider;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Cloud implements InformationProvider {
	private ContinuousSpace<Object> space;	//actual terrain
	private Grid<Object> grid;	//actual grid
	private Wind wind;	//reference to global wind
	private double tank;	//water for each cell this cloud carries
	private double rain;	//number of water which rains in each iteration
	private double maxRain;	//
	private double minRain;
	private Random random;
	private Context<Object> context;
	private ArrayList<Point> futureCells;
	private double inertia;
	
	public Cloud(Context<Object> context, Wind wind, ContinuousSpace<Object> space, Grid<Object> grid, double tank, double maxRain, double minRain) {
		super();
		this.context = context;
		this.wind = wind;
		this.space = space;
		this.grid = grid;
		this.tank = tank;
		this.random = new Random();
		this.rain = minRain+random.nextDouble()*(maxRain-minRain);
		this.maxRain = maxRain;
		this.minRain = minRain;
		this.futureCells = new ArrayList<Point>();
		this.inertia = 0.6; //TODO maybe depend on tank
	}
	
	public void init(int minDim, int maxDim){
		int xDim = random.nextInt(maxDim-minDim)+minDim;
		int yDim = random.nextInt(maxDim-minDim)+minDim;
		//generate futureCells 
		for(int i =-xDim/2;i<=xDim/2;i++){
			for(int j = -yDim/2;j<yDim/2;j++)
				futureCells.add(new Point(j,i));
		}
	}
	
	
	
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	public void step() {
		if(move()){
			addCells();
		}
		changePower();
	}
	
	//called by WaterCells
	public double getRain(){
		if(tank>=rain){
			tank-=rain;
			return rain;
		}
		else{
			return 0;
		}
	}
	
	public double getInertia() {
		return inertia;
	}

	//add cells to map
	private void addCells() {
		NdPoint myPoint = space.getLocation(this);
		double x = myPoint.getX();
		double y = myPoint.getY();
		for(int i =0;i<futureCells.size();i++){
			Point p = futureCells.get(i);
			if(onMap(x+p.x, y+p.y)){
				WaterCell wc = new WaterCell(space, grid, wind, this);
				context.add(wc);
				space.moveTo(wc, x+p.x, y+p.y);
				grid.moveTo(wc, (int)(x+p.x), (int)(y+p.y));
				futureCells.remove(p);
				i--;
			}
		}
	}
	
	//change power of rain 
	private void changePower(){
		this.rain+=random.nextGaussian();
		if(this.rain>maxRain)
			this.rain = maxRain;
		else if (this.rain < minRain)
			this.rain = minRain;
	}
	
	// move according wind direction/speed
	private boolean move() {
		// get actual point
		NdPoint myPoint = space.getLocation(this);
		// get angle from wind
		double angle = this.wind.getWindDirection();
		// cloud is a little bit slower than wind
		double distance = this.inertia * this.wind.getSpeed();
		// look if new position is on map
		double newX = myPoint.getX() + Math.cos(angle) * distance;
		double newY = myPoint.getY() + Math.sin(angle) * distance;
		if (!onMap(newX, newY)) {
			Context<Object> context = ContextUtils.getContext(this);
			context.remove(this);
			return false;
		} else {
			// move in continuous space
			space.moveByVector(this, distance, angle, 0);
			myPoint = space.getLocation(this);
			// move in grid
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());
			return true;
		}
	}
	
	private boolean onMap(double x, double y){
		return x >= 0 && y >= 0 && x < grid.getDimensions().getWidth()
				&& y < grid.getDimensions().getHeight();
	}

	@Override
	public CloudInformation getInformation() {
		GridPoint location = grid.getLocation(this);
		return new CloudInformation(location.getX(), location.getY(), rain);
	}
	
	public static class CloudInformation extends Information {

		private double rain;
		
		private CloudInformation(Integer positionX, Integer positionY, double rain) {
			super(positionX, positionY);
			this.rain = rain;
		}

		/**
		 * "Remove" information constructor.
		 * 
		 * @param positionX
		 * @param positionY
		 */
		public CloudInformation(Integer positionX, Integer positionY)
		{
			super(positionX, positionY, true);
		}
		
		public double getRain() {
			return rain;
		}
	}
}
