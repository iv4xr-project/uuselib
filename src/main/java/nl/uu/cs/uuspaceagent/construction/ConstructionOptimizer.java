package nl.uu.cs.uuspaceagent.construction;

import java.util.ArrayList;
import java.util.List;

import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
import static nl.uu.cs.uuspaceagent.TestUtils.console;

public abstract class ConstructionOptimizer {
	
	UUSeAgentState agentState;
	ConstructionPlanner planner;
	
	public ConstructionOptimizer(UUSeAgentState agentState) {
		this.agentState = agentState;
	}
	
	public List<DPos3> SortCells(List<DPos3> cells) {
		List<DPos3> sortedCells = new ArrayList<DPos3>(cells);
		return sortedCells;
	}
	
	
	public static ConstructionOptimizer DFS(UUSeAgentState agentState) {
		return new DFS(agentState);
	}
	
	public static ConstructionOptimizer DFS2(UUSeAgentState agentState) {
		return new DFS2(agentState);
	}
}

class DFS extends ConstructionOptimizer {
	

	public DFS(UUSeAgentState agentState) {
		super(agentState);
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

class DFS2 extends DFS {
	

	public DFS2(UUSeAgentState agentState) {
		super(agentState);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<DPos3> SortCells(List<DPos3> cells) {
		cells = super.SortCells(cells);

		cells.sort((pos1, pos2) -> Integer.compare(
				pos1.y, 
				pos2.y));
		
		return cells;
	}
}