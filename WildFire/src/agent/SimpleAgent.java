package agent;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import environment.Fire;
import environment.Fire.FireInformation;
import environment.Wood;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.space.grid.GridPoint;

public class SimpleAgent extends ForesterAgent {

	public SimpleAgent(ContinuousSpace<Object> space, Grid<Object> grid,
			double speed, double extinguishRate) {
		super(space, grid, speed, extinguishRate);
	}

	@Override
	protected void decideOnActions() {
		GridPoint location = grid.getLocation(this);
		
		GridDimensions dimensions = grid.getDimensions();
		int gridHeight = dimensions.getHeight();
		int gridWidth = dimensions.getWidth();
		
		List<FireInformation> fireInformationList = updateFireBelief();
		for(FireInformation fireInformation : fireInformationList)
		{
			//send to a fraction of the entire forest grid
			communicationTool.setSendingRange((int)((gridHeight+gridWidth)/20));
			communicationTool.sendInformation(fireInformation);
		}
		
		//avoid being on burning forest tiles
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
			//Move towards closest fire known from belief
			Collection<FireInformation> allInformation = belief.getAllInformation(FireInformation.class);
			
			int minDistance = Integer.MAX_VALUE;
			FireInformation fireTarget = null;
			for(FireInformation fireInformation : allInformation)
			{
				if(fireInformation.isEmptyInstance())
				{
					continue;
				}
				
				int xDiff = Math.abs(location.getX() - fireInformation.getPositionX());
				int yDiff = Math.abs(location.getY() - fireInformation.getPositionY());
				if(xDiff + yDiff < minDistance)
				{
					fireTarget = fireInformation;
				}
			}
			if(fireTarget == null)
			{
				Random random = new Random();
				GridPoint gp = new GridPoint(random.nextInt(grid.getDimensions().getWidth()), random.nextInt(grid.getDimensions().getHeight()));
				moveTowards(gp);
			}
			else
			{
				moveTowards(new GridPoint(fireTarget.getPositionX(), fireTarget.getPositionY()));
			}
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
