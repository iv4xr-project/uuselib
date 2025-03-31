package nl.uu.cs.uuspaceagent.construction;

import java.util.ArrayList;
import java.util.List;

import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
import static nl.uu.cs.uuspaceagent.TestUtils.console;

public abstract class ConstructionOptimizer {
	
	UUSeAgentState agentState;
	ConstructionPlanner planner;
	
	public ConstructionOptimizer() {
	}
	
	public List<DPos3> SortCells(List<DPos3> cells) {
		List<DPos3> sortedCells = new ArrayList<DPos3>(cells);
		return sortedCells;
	}
	
	
	public static ConstructionOptimizer DFS( int version) {
		switch (version) {
			case 1:
				return new DFS1();
			case 2:
				return new DFS2();
			case 3:
				return new DFS3();
		}
		return null;
	}
	
	public static ConstructionOptimizer BFS( int version) {
		switch (version) {
			case 1:
				return new BFS1();
				
			case 2:
				return new BFS2();
		}
		return null;
	}
}

/**
 * Basic DFS (Depth-first search) optimizer that sorts cells based on their distance to the previously placed block.
 */
class DFS1 extends ConstructionOptimizer {
	

	public DFS1() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DPos3> SortCells(List<DPos3> cells) {
		cells = super.SortCells(cells);
		
		cells.sort((pos1, pos2) -> Float.compare(
				getScore(pos1), 
				getScore(pos2)));
		return cells;
	}
	
	float getScore(DPos3 pos) {
		return DPos3.distSq(pos, planner.latestBlock);
	}
}

/**
 * Adaptation of DFS1 that prioritizes building layer by layer.
 */
class DFS2 extends DFS1 {
	

	public DFS2() {
		// TODO Auto-generated constructor stub
	}
	
	float getScore(DPos3 pos) {
		var score = super.getScore(pos);
		score += pos.y;
		return score;
	}
}

/**
 * Adaptation of DFS3 that optimizes for distance to player.
 */
class DFS3 extends DFS2 {
	

	public DFS3() {
		// TODO Auto-generated constructor stub
	}
	
	float getScore(DPos3 pos) {
		var score = super.getScore(pos);
		score += Vec3.dist(planner.getWorldLocationFromCell(pos), agentState.worldmodel.position)/planner.cellSize;
		return score;
	}
}

/**
 * Basic BFS (Breadth-first search) optimizer that sorts cells based on their distance to the origin.
 */
class BFS1 extends ConstructionOptimizer {
	

	public BFS1() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DPos3> SortCells(List<DPos3> cells) {
		cells = super.SortCells(cells);
		
		cells.sort((pos1, pos2) -> Float.compare(
				getScore(pos1), 
				getScore(pos2)));
		return cells;
	}
	
	float getScore(DPos3 pos) {
		return DPos3.distSq(pos, planner.blueprint.originCell);
	}
}

/**
 * Adaptation of DFS1 that prioritizes building layer by layer.
 */
class BFS2 extends BFS1 {
	

	public BFS2() {
		// TODO Auto-generated constructor stub
	}
	
	float getScore(DPos3 pos) {
		var score = super.getScore(pos);
		score += pos.y*0.5f;
		return score;
	}
}

