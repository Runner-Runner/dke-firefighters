package agent;

import java.util.List;
import java.util.Random;

import environment.Fire;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class SimpleAgent extends ForesterAgent {

	public SimpleAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed) {
		super(space, grid, speed);
	}

	@Override
	protected void decideOnActions() {
		//avoid being on burning forest tiles
		if(isOnBurningTile())
		{
			//move to first nonburning tile
			GridPoint location = grid.getLocation(this);
			GridCellNgh<Fire> nghCreator = new GridCellNgh<>(grid, location,
					Fire.class, 1, 1);
			List<GridCell<Fire>> gridCells = nghCreator.getNeighborhood(true);
			GridPoint fleeingPoint = null;
			for (GridCell<Fire> cell : gridCells) {
				if (cell.size() == 0) {
					fleeingPoint = cell.getPoint();
					break;
				}
			}
			// all neighbor tiles on fire - move to first one
			if(fleeingPoint == null)
			{
				fleeingPoint = gridCells.get(0).getPoint();
			}
			moveTowards(fleeingPoint);
			return;
		}
		
		GridPoint fireGridPoint = findFireInNeighborhood();
		if(fireGridPoint != null)
		{
			extinguishFire(fireGridPoint);
		}
		else
		{
			//TODO integrate communication, move in direction of known fire positions
			Random random = new Random();
			GridPoint gp = new GridPoint(random.nextInt(grid.getDimensions().getWidth()), random.nextInt(grid.getDimensions().getHeight()));
			moveTowards(gp);
		}
	}

	/**
	 * Searches for a fire in the Moore neighborhood and returns the first that is found.
	 * 
	 * @return GridPoint containing a fire or null, if there is no fire in the Moore neighborhood
	 */
	protected GridPoint findFireInNeighborhood() {
		GridPoint location = grid.getLocation(this);
		GridCellNgh<Fire> nghCreator = new GridCellNgh<>(grid, location,
				Fire.class, 1, 1);
		List<GridCell<Fire>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Fire> cell : gridCells) {
			if (cell.size() > 0) {
				return cell.getPoint();
			}
		}
		return null;
	}
}
