package nl.uu.cs.uuspaceagent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static nl.uu.cs.uuspaceagent.TestUtils.console;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;
import nl.uu.cs.aplib.utils.Pair;
import spaceEngineers.model.DefinitionId;
import static nl.uu.cs.aplib.AplibEDSL.* ;


public class ConstructionPlanner implements Iterator<GoalStructure>{

	// The size of 1 'large' size block, the size of the grid used.
	float cellSize = 2.5f;
	
	// Container for all the information about the construction
	public Blueprint blueprint;
	
	// World location from which the construction should spread out.
	public Vec3 location;
	Vec3 gridOffset;
	
	UUSeAgentState agentState;
	
	
	List<Pair<DPos3, GoalStructure>> pendingBlocks;
	
	public ConstructionPlanner(Blueprint blueprint, Vec3 originLocation, WorldEntity grid, UUSeAgentState agentState) {
		this.agentState = agentState;
		
		var gridOrigin = grid.position;
		gridOffset = new Vec3(
				gridOrigin.x % cellSize, 
				gridOrigin.y % cellSize, 
				gridOrigin.z % cellSize);
		
		var snappedLocation = new Vec3(
				Math.round((originLocation.x - gridOffset.x) / cellSize) * cellSize + gridOffset.x,
				Math.round((originLocation.y - gridOffset.y) / cellSize) * cellSize + gridOffset.y,
				Math.round((originLocation.z - gridOffset.z) / cellSize) * cellSize + gridOffset.z);
		this.location = snappedLocation;
		
		this.blueprint = blueprint;
		pendingBlocks = new ArrayList<Pair<DPos3, GoalStructure>>();
	}
	
	public Vec3 getWorldLocationFromCell(DPos3 cellPosition) {
		// TODO Implement conversion from cell position in grid to real world coordinates.
		var offsetToCell = DPos3.sub(cellPosition, blueprint.originCell);
		console(offsetToCell.toString());
		var cellLocation = new Vec3(
				location.x + offsetToCell.x*cellSize,
				location.y + offsetToCell.y*cellSize,
				location.z + offsetToCell.z*cellSize);
		
		return cellLocation;
	}
	
	public DPos3 getCellLocationFromWorld(Vec3 worldPosition) {
		
		//This could possibly still behalve incorrectly but seems to work for now.
		var cellLocation = new DPos3(
				(int) Math.round((worldPosition.x - location.x - gridOffset.x) / cellSize) + blueprint.originCell.x,
				(int) Math.floor((worldPosition.y - location.y - gridOffset.y) / cellSize) + blueprint.originCell.y,
				(int) Math.round((worldPosition.z - location.z - gridOffset.z) / cellSize) + blueprint.originCell.z);
		
		return cellLocation;
	}

	
	public GoalStructure getConstructionGoal(DPos3 cellPosition) {
		var blockLocation = getWorldLocationFromCell(cellPosition);
		DefinitionId blockDefinition = blueprint.getDefinitionAtCell(cellPosition);
		if (blockDefinition == null) {
			console("*** No block at cell " + cellPosition.toString());
			return FAIL();
		}
		WorldEntity entity = blueprint.getEntityAtCell(cellPosition);
		if (entity != null) {
			console("*** entity " + entity.type + " already built at cell " + cellPosition.toString()); 
		}
		
		var G = DEPLOY(UUGoalLib.placedBlockAt(blockLocation, blockDefinition));
		return G;
	}
	
	/**
	 * Updates the current list of active goals (list will usually only contain 1 goal)
	 * @return A boolean representing whether the goals were updated succesfully. If false, construction should be aborted.
	 */
	boolean updatePendingGoals() {
		
		// Add completed blocks to the blueprint for progress tracking
		for (Pair<DPos3, GoalStructure> posAndGoal : pendingBlocks) {
			GoalStructure goal = posAndGoal.snd;
			DPos3 cellPosition = posAndGoal.fst;
			
			if (goal.getStatus().success()) {
				var worldLocation = getWorldLocationFromCell(cellPosition);
				DefinitionId expectedId = blueprint.getDefinitionAtCell(cellPosition);
				
				// Find the actual entity of the newly placed block.
				WorldEntity newBlock = SEBlockFunctions.findClosestBlock(agentState.worldmodel, (WorldEntity we) -> {
					
					// If it isn't the right type of block, skip it.
					if (!we.getProperty("blockType").toString().equals(expectedId.getType()))
						return false;
					
					// If it isn't within the range of the intended area, skip it.
					if (!SEBlockFunctions.pointInsideArea(
							Vec3.sub(we.position, Vec3.one()), 
							Vec3.add(we.position, Vec3.one()),
							worldLocation))
						return false;
					return true;
				});
				
				// If block was not found then something went wrong during placement and construction should be aborted.
				if (newBlock == null) {
					console("*** Block was not succesfully placed at " + cellPosition.toString());
					return false;
				}
				
				blueprint.addEntity(cellPosition, newBlock);
			}
		}
		
		// Remove finished goals
		pendingBlocks.removeIf((Pair<DPos3, GoalStructure> posAndGoal) -> posAndGoal.snd.getStatus().success());
		return true;
	}
	
	/**
	 * @return A list of blocks that, given the current state of construction, can be placed.
	 * This means that, among others, a block needs to have at least 1 existing and placed neigbour.
	 */
	List<DPos3> placeableCells(){
		var unplacedCells = blueprint.getUnplacedCells();
		
		// For each unplaced cell, check whether it can be placed.
		var placeableCells = unplacedCells.stream().filter((DPos3 pos) -> {
			var neighborEntities = blueprint.getNeighbourEntities(pos);
			
			// A block can't be placed if it has no placed neighbors or is completely surrounded on all sides.
			if (neighborEntities.size() == 0 || neighborEntities.size() == 6) return false;
			
			// TODO: add a check that removes blocks that would make construction of other blocks impossible. 
			
			return true;
		}).toList();
		
		return placeableCells;
	}
	
	@Override
	public boolean hasNext() {
		boolean succesfulUpdate = updatePendingGoals();
		if (!succesfulUpdate) return false;
		
		var placeableCells = placeableCells();
		
		// TODO check if their is a next step possible
		return placeableCells.size() > 0;
	}

	@Override
	public GoalStructure next() {
		
		// If updating currently active goals fails, 
		// then a fatal error has occurred and we cancel construction.
		boolean succesfulUpdate = updatePendingGoals();
		if (!succesfulUpdate)
			return FAIL();
		
		//TODO: add a inventory check in the case of survival mode
		
		
		// Get possible blocks to place
		var placeableCells = placeableCells();
		if (placeableCells.size() == 0) return null;
		
		//TODO sort the placeable cells for BFS or DFS
		
		// Get the goal for the next block
		DPos3 cellPosition = placeableCells.get(0);
		GoalStructure G = getConstructionGoal(cellPosition);
		
		// Add goal to pending list
		pendingBlocks.add(new Pair<>(cellPosition,G));
		
		return G;
	}
	
}

