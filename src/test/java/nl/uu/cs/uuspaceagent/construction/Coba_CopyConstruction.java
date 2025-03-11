package nl.uu.cs.uuspaceagent.construction;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
import spaceEngineers.model.typing.DefinitionIds;
import spaceEngineers.model.typing.DefinitionIds.CubeBlock;

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
        			
        			//Find grid
        	        WorldEntity grid = null;
        	        for (var e : agentState.worldmodel.elements.keySet())
        	        {
        	        	if (agentState.worldmodel.elements.get(e).type == "grid")
        	        	{
        	        		grid = agentState.worldmodel.elements.get(e);
        	        	}
        	        } 
        			
        	        Blueprint blueprint = new Blueprint(new DefinitionId[13][13][13], new DPos3(6,0,6));
        			ConstructionPlanner planner = new ConstructionPlanner(blueprint, origin, grid, agentState);
        			
        			// Find all blocks inside the construction area
        			var blocksInArea = SEBlockFunctions.getAllBlocks(agentState.worldmodel).stream().filter((WorldEntity we) -> {
        				
        				// Check if block is in construction area.
        				var inArea = SEBlockFunctions.pointInsideArea(corner1, corner2, we.position);
        				return inArea;
        				
        			}).toList();
        			
        			for (var block : blocksInArea) {
        				var cellPosition = planner.getCellLocationFromWorld(block.position);
        				console(block.properties.keySet().toString());
        				DefinitionId definition = DefinitionId.Companion.cubeBlock(block.getProperty("blockType").toString());
        				blueprint.addDefinition(cellPosition, definition);
        			}
        			
        			
        			
        			String filePath = "assets/se-worlds/ConstructionPlatform/";
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
