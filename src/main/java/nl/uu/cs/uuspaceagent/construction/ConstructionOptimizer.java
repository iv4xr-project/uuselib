package nl.uu.cs.uuspaceagent.construction;

import java.util.ArrayList;
import java.util.List;

import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.UUSeAgentState;

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
		}
		return null;
	}
}

/**
 * Basic DFS optimizer that sorts cells based on their distance to the previously placed block.
 */
class DFS1 extends ConstructionOptimizer {
	

	public DFS1() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DPos3> SortCells(List<DPos3> cells) {
		cells = super.SortCells(cells);
		
		cells.sort((pos1, pos2) -> Integer.compare(
				DPos3.distSq(pos1, planner.latestBlock), 
				DPos3.distSq(pos2, planner.latestBlock)));
		return cells;
	}
}

/**
 * Adaptation of DFS1 that prioritizes building layer by layer.
 */
class DFS2 extends DFS1 {
	

	public DFS2() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DPos3> SortCells(List<DPos3> cells) {
		cells = super.SortCells(cells);

		// TODO: tweak this so that there is more nuance to the prioritization
		cells.sort((pos1, pos2) -> Integer.compare(
				pos1.y, 
				pos2.y));
		
		return cells;
	}
}