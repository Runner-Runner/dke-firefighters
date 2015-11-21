package agent;

import java.util.List;
import java.util.Random;

import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import environment.Fire;
import environment.Wood;

public class BDIAgent extends ForesterAgent{
	
	
	public BDIAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
	}


	
	private boolean flee(){
		GridPoint location = grid.getLocation(this);
		if(isOnBurningTile())
		{
			GridPoint fleeingPoint = null;
			//move to burned wood tile (secure space) if possible
			GridCellNgh<Wood> nghWoodCreator = new GridCellNgh<>(grid, location,
					Wood.class, 1, 1);
			List<GridCell<Wood>> woodGridCells = nghWoodCreator.getNeighborhood(false);
			for(GridCell<Wood> cell : woodGridCells)
			{
				if (cell.size() == 0) {
					fleeingPoint = cell.getPoint();
					break;
				}
			}
			if(fleeingPoint == null)
			{
				//otherwise, move to first non-burning tile
				GridCellNgh<Fire> nghFireCreator = new GridCellNgh<>(grid, location,
						Fire.class, 1, 1);
				List<GridCell<Fire>> fireGridCells = nghFireCreator.getNeighborhood(false);
				for (GridCell<Fire> cell : fireGridCells) {
					if (cell.size() == 0) {
						fleeingPoint = cell.getPoint();
						break;
					}
				}
				
				// all neighbor tiles on fire - move to first one
				if(fleeingPoint == null)
				{
					fleeingPoint = fireGridCells.get(0).getPoint();
				}
			}
			return true;
		}
		return false;
	}




	@Override
	public void doRequests() {
		GridPoint location = grid.getLocation(this);
		
		GridCellNgh<Fire> nghFire = new GridCellNgh<>(grid, location,
				Fire.class, 1, 1);
		List<GridCell<Fire>> fires = nghFire.getNeighborhood(true);
		
		GridCellNgh<ForesterAgent> nghAgents = new GridCellNgh<>(grid, location,
				ForesterAgent.class, 1, 1);
		List<GridCell<ForesterAgent>>  agents = nghAgents.getNeighborhood(true);
		
		//there are more fires than agents -> call 911
		if(fires.size()>agents.size()-1){
			communicationTool.setSendingRange(100);
			this.costs+=communicationTool.sendRequest(new ActionRequest(1, location.getX(), location.getY(), new Extinguish(), this.communicationId));
		}
	}

	@Override
	public void checkResponds() {
		for(Information i:messages){
			this.belief.addInformation(i);
		}
		messages.clear();
		//TODO check offers and send confirmations
	}

	@Override
	public void checkConfirmations() {
		// TODO if there is a confirmation change your intention
		
	}

	@Override
	public void doActions() {
		if (!flee()) {
			// TODO act with respect to your intention (move, extinguish, wetline, woodcutting, check weather)
			// go for a walk
			GridPoint location = grid.getLocation(this);
			Random r = new Random();
			int x = location.getX() + r.nextInt(3) - 1;
			int y = location.getY() + r.nextInt(3) - 1;
			moveTowards(new GridPoint(x, y));
		}
	}
}
