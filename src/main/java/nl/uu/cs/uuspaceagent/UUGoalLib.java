package nl.uu.cs.uuspaceagent;

import environments.SeEnvironmentKt;
import eu.iv4xr.framework.mainConcepts.ObservationEvent;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import spaceEngineers.iv4xr.goal.GoalBuilder;
import spaceEngineers.iv4xr.goal.TacticLib;
import nl.uu.cs.aplib.mainConcepts.*;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.GoalsCombinator;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;

import static nl.uu.cs.aplib.AplibEDSL.* ;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.SEBlockFunctions.BlockSides;
import spaceEngineers.model.Block;
import spaceEngineers.model.DefinitionId;
import spaceEngineers.model.Observation;
import spaceEngineers.model.ToolbarLocation;

import java.awt.SecondaryLoop;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static nl.uu.cs.uuspaceagent.TestUtils.console;

public class UUGoalLib {
	
	/**
     * REPEATwith(iter) implements a more generic sequencer. It takes the next goal in the iterator, 
     * completes it, and then continues to the goal after that. This repeats until the iterator runs out
     * items. This approach allows an external system to dynamically generate an unlimited amount of goals on demand.
     */
    public static <AgentState extends SimpleState> GoalStructure REPEATwith(Iterator<GoalStructure> gs) {
    	if (!gs.hasNext()) return SUCCESS();
    	
    	var nextGoal = DEPLOY((AgentState state) -> {
    		return REPEATwith(gs);
    	});
    	
    	return SEQ(gs.next(), nextGoal);
    	
    }

    /**
     * A goal that is solved when the agent manage to be in some distance close to a
     * given destination. The destination itself should be reachable from the agent
     * current position. The solver for this goal is the tactic navigateToTAC.
     *
     * The goal is aborted if the destination is not reachable.
     */
    public static Function<UUSeAgentState,GoalStructure> closeTo(String goalname, Vec3 targetLocation) {

        if(goalname == null) {
            goalname = "close to location " + targetLocation ;
        }

        String goalname_ = goalname ;

        return (UUSeAgentState state) -> {
            Vec3 targetSquareCenter = state.navgrid.getSquareCenterLocation(state.navgrid.gridProjectedLocation(targetLocation));
            GoalStructure G = goal(goalname_)
                    .toSolve((Pair<Vec3,Vec3> posAndOrientation) -> {
                        var agentPosition = posAndOrientation.fst ;
                        
                        console("Velocity: " + state.velocity().lengthSq());
                        if (state.velocity().lengthSq() > 0.2f)
                        	return false;
                        
                        if (state.jetpackRunning())
                        {
                        	
                        	return Vec3.sub(targetSquareCenter,agentPosition).lengthSq() <= UUTacticLib.THRESHOLD_SQUARED_DISTANCE_TO_SQUARE_FLYING ;
                        } else {
                        	return Vec3.sub(targetSquareCenter,agentPosition).lengthSq() <= UUTacticLib.THRESHOLD_SQUARED_DISTANCE_TO_SQUARE ;
                        }
                        
                    })
                    .withTactic(
                       FIRSTof(UUTacticLib.smartNavigateToTAC(targetLocation), ABORT()) )
                    .lift() ;
                 return G ;

        } ;
    }

    public static Function<UUSeAgentState,GoalStructure> closeTo(Vec3 targetLocation) {
        return closeTo(null,targetLocation) ;
    }

    /**
     * A goal that is solved when the agent manage to get close (the distance is specified by delta) to the center of a
     * specified face (front/back/ left/right) of the nearest block of the specified block-type, within the given
     * radius. The goal fails if there is no such block in the given radius, or if the agent cannot find a path
     * to the closest one.
     *
     * NOTE: for now the block should be a cube, and upright.
     */
    public static Function<UUSeAgentState,GoalStructure> closeTo(TestAgent agent,
                                                                 String blockType,
                                                                 SEBlockFunctions.BlockSides side,
                                                                 float radius,
                                                                 float delta) {
        float sqradius = radius * radius ;

        return closeTo(agent,
                "type " + blockType,
                (UUSeAgentState state) -> (WorldEntity e)
                        ->
                        blockType.equals(e.getStringProperty("blockType"))
                        && Vec3.sub(e.position, state.worldmodel.position).lengthSq() <= sqradius,
                side,
                delta
                ) ;
    }

    public static Function<UUSeAgentState,GoalStructure> closeTo(TestAgent agent,
                                                                 String blockType,
                                                                 SEBlockFunctions.BlockSides side,
                                                                 float delta) {
        return closeTo(agent,
                "type " + blockType,
                (UUSeAgentState state) -> (WorldEntity e)
                        ->
                        blockType.equals(e.getStringProperty("blockType")),
                side,
                delta
        ) ;
    }

    /**
     * Use this to target a block using a generic selector function.
     */
    public static Function<UUSeAgentState,GoalStructure> closeTo(TestAgent agent,
                                                                 String selectorDesc,
                                                                 Function<UUSeAgentState, Predicate<WorldEntity>> selector,
                                                                 SEBlockFunctions.BlockSides side,
                                                                 float delta) {


        return  (UUSeAgentState state) -> {
            WorldEntity block = SEBlockFunctions.findClosestBlock(state.worldmodel,selector.apply(state)) ;
            if (block == null) return FAIL("Navigating autofail; no block can be found: " + selectorDesc) ;

            Vec3 intermediatePosition = SEBlockFunctions.getSideCenterPoint(block,side,delta + 1.5f) ;
            Vec3 goalPosition = SEBlockFunctions.getSideCenterPoint(block,side,delta) ;
            Vec3 blockCenter = (Vec3) block.getProperty("centerPosition") ;

            // because the agent's position is actually its feet position, we take the corresponding
            // positions at the base of the block as goals. So we project goalPosition and intermediatePosition
            // above to positions at the base of the block.
            Vec3 size = SEBlockFunctions.getActualSize(block) ;
            intermediatePosition.y -= size.y * 0.5 ;
            goalPosition.y -= size.y * 0.5 ;

            return SEQ(DEPLOYonce(agent,
                            closeTo("close to a block of property " + selectorDesc + " @"
                            + block.position
                            + " ," + side + ", targeting " + intermediatePosition,
                            intermediatePosition)),
                      veryclose2DTo("very close to a block of property " + selectorDesc + " @"
                              + block.position
                              + " ," + side + ", targeting " + goalPosition,
                              goalPosition),
                      face2DToward("facing towards a block of property " + selectorDesc + " @"
                              + block.position
                              + " ," + side, blockCenter)
                    ) ;
        } ;
    }

    public static Function<UUSeAgentState,GoalStructure> closeToPosition(TestAgent agent,
                                                                 String blockType,
                                                                 SEBlockFunctions.BlockSides side,
                                                                 Vec3 targetPosition,
                                                                 float radius,
                                                                 float delta) {
        float sqradius = radius * radius ;;
        return closeTo(agent,
                "type " + blockType,
                (UUSeAgentState state) -> (WorldEntity e)
                        ->
                        blockType.equals(e.getStringProperty("blockType"))
                                && Vec3.sub(e.position, targetPosition ).lengthSq() <= sqradius,
                side,
                delta
        ) ;
    }


    /**
     * Equip a tool in location k of tool-bar-0
     */
    public static GoalStructure toolEquiped(int k) {
        return lift("Tool equiped",
                action("equip a tool").do1((UUSeAgentState state) -> {
                    state.env().equip(new ToolbarLocation(k,0));
                    return true ;
                })
        ) ;
    }
    /**
     * Equip grinder, assuming it is put in location-0 of the tool-bar-0.
     * @return
     */
    public static GoalStructure grinderEquiped() {
        return lift("Grinder equiped",
                  action("equip grinder").do1((UUSeAgentState state) -> {
                     //state.env().equip(new ToolbarLocation(0,0));
                	 var itemId = new DefinitionId(DefinitionId.PHYSICAL_GUN, "AngleGrinderItem");
                	 state.env().getController().getItems().setToolbarItem(itemId, new ToolbarLocation(0,0));
                     return true ;
                  })
                ) ;
    }

    /**
     * Unequip-tool (so switching to bare-hand).
     */
    public static GoalStructure barehandEquiped() {
        return lift("Barehand equiped",
                action("equip barehand").do1((UUSeAgentState state) -> {
                    state.env().equip(new ToolbarLocation(0,9));
                    return true ;
                })
        ) ;
    }

    public static GoalStructure photo(String fname) {
        return lift("Screenshot made",
                action("Snapping a picture").do1((UUSeAgentState state) -> {
                    state.env().getController().getObserver().takeScreenshot(fname);
                    return true ;
                })
        ) ;
    }


    public static GoalStructure targetBlockOK(TestAgent agent, Predicate<WorldEntity> predicate, boolean abortIfFail) {

        Tactic checkAction = action("checking a predicate")
                .do1(state -> true)
                .on_((UUSeAgentState state) -> {
                    WorldEntity target = state.targetBlock() ;
                    boolean ok = true ;
                    if (target == null) {
                        ok = false ;
                    }
                    else {
                        ok = predicate.test(target) ;
                    }
                    var datacollector = agent.getTestDataCollector() ;
                    if (datacollector != null) {
                        ObservationEvent.VerdictEvent verdict = new ObservationEvent.VerdictEvent(
                                "Checking target block",
                                target == null ? "target is null" : "" + target.type + "@" + target.position,
                                ok
                                ) ;
                        datacollector.registerEvent(agent.getId(),verdict);
                    }
                    return ok ;
                })
                .lift() ;

        Tactic success = action("success").do1((UUSeAgentState state) -> true).lift() ;

        return SEQ(
           // hmm... why should we equip a tool if we just want to check the state of a bloc??
           grinderEquiped(),
           goal("target entity passes a check")
                   .toSolve(b -> true)
                   .withTactic(
                      abortIfFail ? FIRSTof(checkAction,ABORT()) : FIRSTof(checkAction,success))
                   .lift(),
           barehandEquiped()
        ) ;
    }

    public static GoalStructure grinded(TestAgent agent, float targetIntegrity) {

        // changing DEPLOYONCE to DEPLOY. in order to be ablabe to call this goal for different blocks
        GoalStructure grind = DEPLOY(agent, (UUSeAgentState state) -> {
            WorldEntity target = state.targetBlock() ;
            state.worldmodel.elements.get(agent.getId()).properties.put("previousTargetBlock",target );
            //state.assignTargetBlock(target);
            if (target != null) state.previousTargetBlock = target;
            if(target == null) {
                return FAIL("Grinding autofail: there is no target block.") ;
            }
            String targetId = target.id ;
            float precentageTagetIntegrity = 100 * targetIntegrity ;
            float integrityThreshold = ((float) target.getProperty("maxIntegrity")) * targetIntegrity ;

            return goal("block " + targetId + "(" + target.getStringProperty("blockType") + ") is grinded to integrity <= " + precentageTagetIntegrity + "%")
                        .toSolve((WorldEntity e) -> e == null || ((float) e.getProperty("integrity") <= integrityThreshold))
                        .withTactic(action("Grinding")
                                .do1((UUSeAgentState st) -> {
                                    UUTacticLib.grind(state,50);
                                    Observation rawGridsAndBlocksStates = st.env().getController().getObserver().observeBlocks() ;
                                    WorldModel gridsAndBlocksStates = SeEnvironmentKt.toWorldModel(rawGridsAndBlocksStates) ;
                                    return SEBlockFunctions.findWorldEntity(st.worldmodel,targetId) ;
                                })
                        .lift())
                    .lift() ;
        }) ;

        GoalStructure stopGrinding = lift("Grinding stopped",
                action("stop grinding")
                     .do1((UUSeAgentState st) -> {
                         st.env().endUsingTool();
                         return true ;
                      })
                ) ;

        return SEQ(grinderEquiped(), grind, stopGrinding, barehandEquiped()) ;
    }


    public static GoalStructure veryclose2DTo(String goalname, Vec3 p) {
        if (goalname == null) {
            goalname = "very close to " + p ;
        }
        return goal(goalname)
                .toSolve((Float square_distance) -> {
                    System.out.println(">> sq-dist " + square_distance ) ;
                    return square_distance <= UUTacticLib.THRESHOLD_SQUARED_DISTANCE_TO_POINT ;
                })
                .withTactic(FIRSTof(UUTacticLib.straightline2DMoveTowardsACT(p).lift() , ABORT()))
                .lift() ;
    }

    public static GoalStructure face2DToward(String goalname, Vec3 p) {
        if (goalname == null) {
            goalname = "face towards " + p ;
        }
        return goal(goalname)
                .toSolve((Float cos_alpha) -> 1 - cos_alpha <= 0.01)
                .withTactic(FIRSTof(UUTacticLib.yTurnTowardACT(p).lift() , ABORT()))
                .lift() ;
    }
    
    public static GoalStructure faceToward(String goalname, Vec3 p) {
        if (goalname == null) {
            goalname = "face towards " + p ;
        }
        return goal(goalname)
                .toSolve((Float cos_alpha) -> {
                	console("cos alpha =" + String.valueOf(cos_alpha));
                	return 1 - cos_alpha <= 0.001f;
                })
                .withTactic(FIRSTof(UUTacticLib.TurnTowardACT(p).lift(), ABORT()))
                .lift() ;
    }


    public static boolean findItemPredicate(UUSeAgentState st, String blockType){
            List<WorldEntity> blocks =  SEBlockFunctions.getAllBlocks(st.worldmodel).stream().filter(e -> blockType.equals(e.getStringProperty("blockType"))).collect(Collectors.toList());
            System.out.println("number of blocks" + blocks.size()  );
            for(var block : blocks) {
                System.out.println("Candidates: " + block.id + " type and properties" + block);
            }
            var numberofBlocks = SEBlockFunctions.getAllBlocks(st.worldmodel);
//            for(var block : numberofBlocks) {
//                System.out.println("All blockes Candidates: " + block.id + " type and properties" + block.getStringProperty("blockType"));
//            }
            //get blocks in different way
//                    Observation rawGridsAndBlocksStates = st.env().getController().getObserver().observeBlocks() ;
//                    WorldModel gridsAndBlocksStates = SeEnvironmentKt.toWorldModel(rawGridsAndBlocksStates) ;
//                    var candidates = SEBlockFunctions.getAllBlocks(gridsAndBlocksStates);
//                    for(var block : SEBlockFunctions.getAllBlocks(gridsAndBlocksStates)) {
//                        if(block.getProperty("blockType").equals(blockType)) i++;
//
//                    }

            //print distance to each block
//                     candidates.forEach(e ->
//                            System.out.println("distance: "+  Vec3.dist(e.position, st.wom.position) + " block type" + e.getStringProperty("blockType") +  "position: " + e.position)
//                     );


            if(blocks.size()>0) return false;
            return true ;
    }


    public static GoalStructure doorInteracted(TestAgent agent) {

        return  DEPLOY(agent, (UUSeAgentState state) -> {

            WorldEntity target = SEBlockFunctions.findClosestBlock(state.worldmodel, "LargeBlockSlideDoor", 10) ;
            System.out.println("** door state: " + PrintInfos.showWorldEntity(target));


            Block targetBlock = state.env().getController().getObserver().observe().getTargetBlock() ;
            if (target != null) state.previousTargetBlock = target;
            return goal("block is interacted" )
                    .toSolve((Boolean e)  ->{
                        return e;
                    })
                    .withTactic(
                            SEQ(
                            UUTacticLib.doorInteracted( state,targetBlock)
                            ,UUTacticLib.observeIfBlockIsOpen(state,targetBlock)
                            )
                    )
                    .lift() ;
        }) ;
    }

    public static Function<UUSeAgentState, GoalStructure> placedBlockAt(Vec3 blockLocation, DefinitionId itemId){
    	
    	return (UUSeAgentState state) -> {
    		
    		// lookTarget is the nearest face to the where the block should be placed
    		Pair<BlockSides, WorldEntity> lookTarget = SEBlockFunctions.findClosestFace(state.worldmodel, state, blockLocation);
    		Vec3 lookLocation = SEBlockFunctions.getSideCenterPoint(lookTarget.snd, lookTarget.fst, 0.05f);
    		
    		// Find the best spot for the agent to stand when placing the block.
    		console("looking for empty neighbor near " + lookTarget.fst);
    		var destinationCandidates = SEBlockFunctions.findEmptyNeighbor(
    				state.navgrid,
    				Vec3.add(Vec3.sub(blockLocation, new Vec3(0, 1.8f/2, 0)), 
    						SEBlockFunctions.getSideOffset(lookTarget.snd, lookTarget.fst, 0.25f)), 
    				lookTarget.snd,
    				lookTarget.fst);
    		destinationCandidates.sort((v1, v2) -> Float.compare(
            		Vec3.sub(v1, state.worldmodel.position).lengthSq(),
            		Vec3.sub(v2, state.worldmodel.position).lengthSq()
            		));
    		console("destinationCandidates: " + destinationCandidates.toString());
    		Vec3 playerDestination = destinationCandidates.getFirst();
    		
    		state.navgrid.enableFlying = true;
    		if (Math.abs(playerDestination.y - state.navgrid.origin.y) < 2 && 
    				Math.abs(state.worldmodel.position.y - state.navgrid.origin.y) < 2)
    		{
    			state.navgrid.enableFlying = false;
    			playerDestination.y = state.navgrid.origin.y + 0.1f;
    		}
    		
    		
    		// Goal to move within placement/view range of the target.
    		GoalStructure nearLookTarget = DEPLOY(closeTo(playerDestination));
    		
    		// Goal to look at the face of neighboring block
    		GoalStructure lookAtTarget = faceToward("look towards neighbor side", lookLocation);

    		// Goal to actually place block from inventory.
            GoalStructure blockPlaced =  goal("place block at")
            		.toSolve((Boolean e) -> {
            			return e;
            		})
            		.withTactic(
            				UUTacticLib.equipAndPlace(itemId)
    				)
            		.lift();
            
            GoalStructure welded = blockWelded(blockLocation);
            
            		
            /* Execution sequence:
             * 1. Move to the adjacent spot
             * 2. Look at the face of a nearby block that is closest to the intended destination
             * 3. Equip and use block from inventory
             * 4. Equip empty hand.
             */
            return SEQ(
            		nearLookTarget,
            		lookAtTarget,
            		state.inSurvival ? SEQ(blockPlaced, welded) : blockPlaced
            		);
        } ;		
    }
    
    public static GoalStructure blockWelded(Vec3 blockLocation) {
    	
    	GoalStructure G = SEQ(faceToward("face block to weld", blockLocation),
        		goal("weld block")
        		.toSolve((Boolean e) -> {
        			return e;
        		})
        		.withTactic(SEQ(
        				UUTacticLib.equip(new DefinitionId(DefinitionId.PHYSICAL_GUN, "Welder4Item")),
        				action("weld block").do1((UUSeAgentState state) -> {
            				
            				if ((float)state.targetBlock().getProperty("integrity") == 
            						(float)state.targetBlock().getProperty("maxIntegrity")) {
            					return true;
            				}
            				
            				for(int k=0; k<20; k++) {
            					state.env().beginUsingTool();
            		        }
            				
            				return null;
            			}).lift()
				))
        		.lift()
    		);
    	
    	return G;
    }
    
    public static Function<UUSeAgentState, GoalStructure> accessedBlockInventory(WorldEntity entity){
    	return (UUSeAgentState state) -> {
    		
    		console("looking for empty neighbor near " + entity.position);
    		var destinationCandidates = SEBlockFunctions.findEmptyNeighbor(
    				state.navgrid,
    				Vec3.sub(entity.position, new Vec3(0, 1.8f/2, 0)), 
    				entity, null);
    		console("destinationCandidates: " + destinationCandidates.toString());
    		destinationCandidates.sort((v1, v2) -> Float.compare(
            		Vec3.sub(v1, state.worldmodel.position).lengthSq(),
            		Vec3.sub(v2, state.worldmodel.position).lengthSq()
            		));
    		Vec3 playerDestination = destinationCandidates.getFirst();
    		
    		
    		state.navgrid.enableFlying = true;
    		if (Math.abs(playerDestination.y - state.navgrid.origin.y) < 2 && Math.abs(state.worldmodel.position.y - state.navgrid.origin.y) < 2)
    		{
    			state.navgrid.enableFlying = false;
    			playerDestination.y = state.navgrid.origin.y + 0.1f;
    		}
    		
    		GoalStructure nearLookTarget = DEPLOY(closeTo(playerDestination));
    		
    		GoalStructure facingBlock = faceToward("look at center of object", entity.position);
    		
    		GoalStructure openedInventory = goal("openedInventory")
    				.toSolve((Boolean e) -> {
    					return e;
    				})
    				.withTactic(UUTacticLib.openBlockInventory())
    				.lift();
    		
    		return SEQ(
    				lift("close terminal", UUTacticLib.closeTerminal()),
    				nearLookTarget,
    				facingBlock,
    				openedInventory
    				);
		};
	}
    
    public static Function<UUSeAgentState, GoalStructure> rechargedPlayer(){
    	return (UUSeAgentState state) -> {
    		
    		var block = SEBlockFunctions.findClosestBlock(state.worldmodel,
                    e -> "SurvivalKitLarge".equals(e.getStringProperty("blockType")));
    		
    		Vec3 intermediatePosition = SEBlockFunctions.getSideCenterPoint(block,SEBlockFunctions.BlockSides.BACK, 1.5f);
    		Vec3 size = SEBlockFunctions.getActualSize(block) ;
            intermediatePosition.y -= size.y * 0.5 ;
    		
            var interactWithKit = action("interactWithKit").do1((UUSeAgentState state2) -> {
            	
            	for(int k=0; k<30; k++) {
            		state2.env().getController().getCharacter().use();
            	}
            	
            	if (state.oxygen() >= 0.95f && state.hydrogen() >= 0.95f && state.energy() >= 0.95f) {
            		return true;
            	}
            	
            	return null;
            });
            
    		return SEQ(
    				DEPLOY(UUGoalLib.closeTo(intermediatePosition)),
    				faceToward("facing center of survival kit", block.position),
    				lift("interactWithKit", interactWithKit)
    				);
    				
    	};
    }
    
    public static Function<UUSeAgentState, GoalStructure> restockedPlayer(){
    	return (UUSeAgentState state) -> {
    		
    		// Container block to access
            var blocks = SEBlockFunctions.findBlocks(state.worldmodel, (WorldEntity we) -> {
            	return we.getProperty("blockType").toString().contains("Container");
            });
            
            List<GoalStructure> goals = blocks.stream().map((WorldEntity entity) -> {
            	
            	GoalStructure transferItem = goal("withdraw steel plates")
                		.toSolve((Integer transferredCount) -> {
                			return transferredCount > 0 && state.getItemCount(new DefinitionId("Component", "SteelPlate")) >= 25;
                		})
                		.withTactic(FIRSTof(UUTacticLib.withdrawItemToPlayer(new DefinitionId("Component", "SteelPlate")).lift(), ABORT()))
                		.lift();
            	
            	return SEQ(
            			DEPLOY(accessedBlockInventory(entity)),
            			transferItem,
            			lift("close terminal", UUTacticLib.closeTerminal())
            			);
            }).toList();
            
            return FIRSTof(goals.toArray(new GoalStructure[goals.size()]));
    	};
    }
   
}
