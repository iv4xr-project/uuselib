package nl.uu.cs.uuspaceagent.construction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static nl.uu.cs.uuspaceagent.TestUtils.console;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.SEBlockFunctions;
import nl.uu.cs.uuspaceagent.UUGoalLib;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
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
	ConstructionOptimizer optimizer;
	
	// Whether the current location is considered a valid construction site
	boolean validLocation;
	
	// The most recently placed block. At the start this is set to the origin.
	public DPos3 latestBlock;
	
	// Number of times a priority situation arises during the construction.
	int priorityCases = 0;
	
	List<Pair<DPos3, GoalStructure>> pendingBlocks;
	
	public ConstructionPlanner(Blueprint blueprint, Vec3 originLocation, WorldEntity grid, UUSeAgentState agentState, ConstructionOptimizer optimizer) {
		this.agentState = agentState;
		this.optimizer = optimizer;
		if (optimizer != null) {
			this.optimizer.planner = this;
			this.optimizer.agentState = agentState;
		}
				
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
		latestBlock = blueprint.originCell;
		pendingBlocks = new ArrayList<Pair<DPos3, GoalStructure>>();
		
		this.validLocation = isValidConstructionArea();
	}
	
	public Vec3 getWorldLocationFromCell(DPos3 cellPosition) {
		
		var offsetToCell = DPos3.sub(cellPosition, blueprint.originCell);
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
				console("Construction Planner: Adding block " + newBlock.id + " to " + cellPosition.toString());
				blueprint.addEntity(cellPosition, newBlock);
				latestBlock = cellPosition;
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
		
		/**
		 * Priority cells mainly include cells that will be obstructed by others in the future
		 * and should be placed as soon as possible to avoid stuck states.
		 * TODO: consider other approach.
		 */
		List<DPos3> priorityCells = new ArrayList<DPos3>();
		
		// For each unplaced cell, check whether it can be placed.
		var placeableCells = unplacedCells.stream().filter((DPos3 pos) -> {
			
			//if (pos.equals(blueprint.originCell)) return true;
			
			var neighborEntities = blueprint.getNeighbourEntities(pos);

			// If the candidate is at y=0, try to find a block underneath the candidate that isn't part of the structure.
			boolean onGround = false;
			if (pos.y == 0) {
				var groundBlock = SEBlockFunctions.findClosestBlockPosition(
						agentState.worldmodel, 
						getWorldLocationFromCell(DPos3.sub(pos, new DPos3(0,1,0))), 
						0.2f);
				onGround = groundBlock != null;
			}
			
			// A block can't be placed if it has no placed neighbors or is completely surrounded on all sides.
			if ((neighborEntities.size() == 0 && !onGround)|| neighborEntities.size() == 6) return false;
			// NOTE: the check above may not be 100% safe;
			
			// Find priority cells
			int openFaces = 6 - (neighborEntities.size() + (onGround ? 1 : 0));
			var neighborDefinitions = blueprint.getNeighbourDefinitions(pos);
			
			// Check if there is exactly 1 exposed face left that will be covered later
			if (openFaces == 1 && neighborDefinitions.size() > neighborEntities.size())
			{
				priorityCells.add(pos);
			}
			
			//TODO: Check whether the above priority cell logic works.
			//NOTE: This has proven to be much harder than expected due to how complicated it is to create such a situation.
			
			
			return true;
		}).toList();
		
		
		// If there are priority cells, place those before anything else.
		if (priorityCells.size() > 0) {
			console("Number of priority cells: " + priorityCells.size());
			priorityCases += 1;
			return priorityCells;
		}
		
		console("Number of candidate blocks: " + placeableCells.size());
		return placeableCells;
	}
	
	@Override
	public boolean hasNext() {
		
		boolean succesfulUpdate = updatePendingGoals();
		//if (!succesfulUpdate) return false;
		
		var placeableCells = placeableCells();
		
		return placeableCells.size() > 0;
	}

	@Override
	public GoalStructure next() {
		
		// If construction site contains any pre-existing obstructions, abort process.
		if (!validLocation) return FAIL();
		
		// If updating currently active goals fails, 
		// then a fatal error has occurred and we cancel construction.
		boolean succesfulUpdate = updatePendingGoals();
		if (!succesfulUpdate)
			return FAIL();
		
		//TODO: add a inventory check in the case of survival mode
		
		
		// Get possible blocks to place
		var placeableCells = placeableCells();
		
		// If we run out of placeableblocks before the blueprint is finished then something went wrong
		if (placeableCells.size() == 0 && blueprint.getProgress() < 1) return FAIL();
		
		if (optimizer != null) {
			placeableCells = optimizer.SortCells(placeableCells);
		}
		
		// Get the goal for the next block
		DPos3 cellPosition = placeableCells.get(0);
		GoalStructure G = getConstructionGoal(cellPosition);
		
		console("*** next goal is at DPos3" + cellPosition.toString() + "or Vec3" + getWorldLocationFromCell(cellPosition));
		
		// Add goal to pending list
		pendingBlocks.add(new Pair<>(cellPosition,G));
		
		return G;
	}
	
	/**
	 * @return Whether there are any pre-existing obstructions in the construction area.
	 */
	boolean isValidConstructionArea() {
		var size = blueprint.getSize();
		
		for (int x = 0 ; x < size.x ; x++) 
			for (int y = 0 ; y < size.y ; y++) 
				for (int z = 0 ; z < size.z ; z++) {
					DPos3 cellPosition = new DPos3(x, y, z);
					if (blueprint.getDefinitionAtCell(cellPosition) == null) continue;

					var obstruction = SEBlockFunctions.findClosestBlockPosition(
							agentState.worldmodel, 
							getWorldLocationFromCell(cellPosition), 
							0.2f);
					
					if (obstruction != null) {
						console("*** Can't perform construction:");
						console(obstruction.getProperty("blockType").toString() + " obstructing cell " + cellPosition.toString());
						return false;
					}		
		}
		return true;
	}
	
}

