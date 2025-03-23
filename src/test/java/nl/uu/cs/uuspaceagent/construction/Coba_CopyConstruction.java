package nl.uu.cs.uuspaceagent.construction;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.DefinitionId;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import nl.uu.cs.uuspaceagent.Blueprint;
import nl.uu.cs.uuspaceagent.ConstructionPlanner;
import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.SEBlockFunctions;
import nl.uu.cs.uuspaceagent.UUSeAgentState;

class Coba_CopyConstruction {

	@Test
	void test() throws InterruptedException {
		
		
		console("*** start coba...") ;
		var agentAndState = loadSE("ConstructionPlatform") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);
        
        var corner1 = new Vec3(5f, -6.25f, 43.75f);
        var corner2 = new Vec3(37.5f, 20f ,76.25f);
        var origin = new Vec3(21.25f, -5, 60);
        
        GoalStructure G = goal("copy example structure")
        		.toSolve((UUSeAgentState agentState) -> {
        			var additionalInfo = agentState.worldmodel.elements.get(agentState.agentId);
        			boolean dampenersOn = (boolean) additionalInfo.properties.get("dampenersOn");
        			
        			// If dampeners are turned off, copy the construction. (We use dampener state as an arbitrary trigger).
        			if (dampenersOn) return false;
        			
        			console("*** Parsing construction");
        			
        			//Find grid
        	        WorldEntity gridEntity = null;
        	        for (var e : agentState.worldmodel.elements.keySet())
        	        {
        	        	if (agentState.worldmodel.elements.get(e).type == "grid")
        	        	{
        	        		gridEntity = agentState.worldmodel.elements.get(e);
        	        	}
        	        } 
        			
        	        Blueprint blueprint = new Blueprint(new DefinitionId[13][13][13], new DPos3(6,0,6));
        			ConstructionPlanner planner = new ConstructionPlanner(blueprint, origin, gridEntity, agentState);
        			
        			List<Block> blocksInArea = new ArrayList<Block>();
        			for (var g : state.env().getController().getObserver().observeBlocks().getGrids())
        			{
        				for (var b : g.getBlocks()) {
        					if (SEBlockFunctions.pointInsideArea(
        							corner1, 
        							corner2, 
        							SEBlockFunctions.fromSEVec3(b.getPosition())))
        						blocksInArea.add(b);
        				}
        			}
        			
        			
        			for (var block : blocksInArea) {
        				var cellPosition = planner.getCellLocationFromWorld(SEBlockFunctions.fromSEVec3(block.getPosition()));
        				DefinitionId definition = block.getDefinitionId();
        				blueprint.addDefinition(cellPosition, definition);
        			}
        			
        			console("*** Saving construction");
        			
        			String filePath = "assets/blueprints/";
        			try {
						PrintWriter out = new PrintWriter(filePath + "newConstruction.cons");
						out.println(blueprint.getSize());
						out.println(blueprint.originCell);
						
						var size = blueprint.getSize();
						for (int x = 0 ; x < size.x ; x++) 
							for (int y = 0 ; y < size.y ; y++) 
								for (int z = 0 ; z < size.z ; z++) {
									DPos3 cellPosition = new DPos3(x, y, z);
									DefinitionId definition = blueprint.getDefinitionAtCell(cellPosition);
									if (definition == null) continue;
									out.println(cellPosition + ";" + definition.getType() + ";" + definition.getId());
								}
						//TODO serialize the example construction and write to the out.
						
						out.close();
						
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        			
        			
        			return true;
        		})
        		.withTactic(action("return state").do1((UUSeAgentState s) -> s).lift())
        		.lift();
        
        agent.setGoal(G);
        
        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            state.updateState(state.agentId);
                     
            
            Thread.sleep(500);
            turn++ ;
            if (turn >= 1400) break ;
        }
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
	}

}
