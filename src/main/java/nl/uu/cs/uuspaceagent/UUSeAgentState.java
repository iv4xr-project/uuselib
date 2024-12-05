package nl.uu.cs.uuspaceagent;


//import eu.iv4xr.framework.mainConcepts.W3DAgentState;
import environments.SeEnvironment;
import environments.SeEnvironmentKt;
import eu.iv4xr.framework.extensions.pathfinding.AStar;
import eu.iv4xr.framework.extensions.pathfinding.Pathfinder;
import eu.iv4xr.framework.mainConcepts.Iv4xrAgentState;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.agents.State ;
import nl.uu.cs.aplib.utils.Pair;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.Observation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static uuspaceagent.SEBlockFunctions.fromSEVec3;

/**
 * The state of an aplib-agent meant for controlling SE. Notice that
 * the state-class extends Iv4xrAgentState.
 */
public class UUSeAgentState extends Iv4xrAgentState<Void> {

    public String agentId ;
    
    /**
     * A custom-navigation graph; with sparse representation of navigation nodes. Navigable
     * nodes are not literally stored. Instead, we store blocked nodes.
     */
    public NavGrid navgrid = new NavGrid() ;
    /**
     * An A* path-finder. This will be used in conjunction with the navgrid, with the later
     * provides a navigation-graph.
     */
    public Pathfinder<DPos3> pathfinder = new AStar<>() ;
    public List<DPos3> currentPathToFollow = new LinkedList<>();

    public WorldEntity previousTargetBlock;

    /**
     * If this is set to true, top-level grid (e.g. a space-platform) will be stripped
     * out. That is, we will not store such a grid in the world-model, but instead
     * store its direct-children as top-level entities.
     *
     * <p>Default: false
     */
    public boolean stripOutTopLevelGrid = false ;

    /**
     * SE does not seem to send time-stamp, so we will keep track the number of state-updates
     * as a replacement of time-stamp. Alternatively, we can use system-time if the flag
     * useSystemTimeForTimeStamping below is turned-on.
     */
    long updateCount = 0 ;

    /**
     * If this flag is on, we will use system-time (expressed in current ms) as the time
     * stamp (instead of turn-nr). Default is false.
     */
    public boolean useSystemTimeForTimeStamping = false ;

    public UUSeAgentState(String agentId) {
        this.agentId = agentId ;
    }

    @Override
    public SeEnvironment env() {
        return (SeEnvironment) super.env() ;
    }


    WorldEntity agentAdditionalInfo(CharacterObservation obs) {
        Block targetBlock = obs.getTargetBlock();
        WorldEntity agentWE = new WorldEntity(this.agentId, "agentMoreInfo", true) ;
        agentWE.properties.put("velocity", fromSEVec3(obs.getVelocity())) ;
        agentWE.properties.put("gravity", fromSEVec3(obs.getGravity())) ;
        agentWE.properties.put("orientationForward", fromSEVec3(obs.getOrientationForward())) ;
        agentWE.properties.put("orientationUp", fromSEVec3(obs.getOrientationUp())) ;
        agentWE.properties.put("jetpackRunning", obs.getJetpackRunning()) ;
        agentWE.properties.put("dampenersOn", obs.getDampenersOn()) ;
        agentWE.properties.put("helmetEnabled", obs.getHelmetEnabled()) ;
        agentWE.properties.put("currentLightPower", obs.getCurrentLightPower()) ;
        agentWE.properties.put("energy", obs.getEnergy()) ;
        agentWE.properties.put("oxygen", obs.getOxygen()) ;
        agentWE.properties.put("hydrogen", obs.getHydrogen()) ;
        agentWE.properties.put("health", obs.getHealth()) ;
        agentWE.properties.put("displayName", obs.getDisplayName()) ;
        agentWE.properties.put("targetBlock", targetBlock == null ? null : targetBlock.getId()) ;
        agentWE.properties.put("previousTargetBlock", null) ;
       //System.out.println(">>> constructing extra info for agent") ;
        return agentWE ;
    }

    WorldEntity agentInventory(CharacterObservation obs) {
        var inv = obs.getInventory() ;
        WorldEntity invWE = new WorldEntity("inv", "bag", true) ;
        invWE.properties.put("mass",inv.getCurrentMass()) ;
        invWE.properties.put("numOfItems",inv.getItems().size()) ;
        List<Pair<String,Integer>> content = new LinkedList<>() ;
        invWE.properties.put("content", (Serializable) content) ;
        var items = inv.getItems() ;
        for (var I : items) {
            String id = I.getId().getType();
            Integer amount = I.getAmount() ;
            content.add(new Pair<>(id,amount)) ;
        }
        return invWE ;
    }

    @Override
    public void updateState(String agentId) {

        // Deliberately not invoking super-UpdateState !
        // super.updateState(agentId);

        // get the new WOM. Currently it does not include agent's extended properties, so we add them
        // explicitly here:
        WorldModel newWom = env().observe() ;

        //System.out.println(">>>-- agent pos as received from SE:" + newWom.position);
        // HACK: SE gives generated-id to the agent, replace that:
        newWom.agentId = this.agentId ;
        // HACK: because wom that comes from SE has its wom.elements read-only :|
        // WP: this elems seem to be empty??
        var origElements = newWom.elements ;
        //System.out.println(">>> #elements in observed-wom: " + origElements.size()) ;
        newWom.elements = new HashMap<>() ;
        for (var e : origElements.entrySet()) {
            WorldEntity entity = e.getValue() ;
            if (entity.type.equals("grid") && stripOutTopLevelGrid) {
                // a top-level grid, and the option wants to strip it:
                var grid = entity ;
                for (var child : grid.elements.values()) {
                    newWom.elements.put(child.id,child) ;
                }
            }
            else {
                // no stripping:
                newWom.elements.put(e.getKey(),entity) ;
            }
        }

        CharacterObservation agentObs = env().getController().getObserver().observe() ;
        newWom.elements.put(this.agentId, agentAdditionalInfo(agentObs)) ;
        WorldEntity inv =  agentInventory(agentObs) ;
        newWom.elements.put(inv.id,inv) ;

        // The obtained wom also does not include blocks observed. So we get them explicitly here:
        // Well, we will get ALL blocks. Note that S=some blocks may change state or disappear,
        // compared to what the agent currently has it its state.wom.
        Observation rawGridsAndBlocksStates = env().getController().getObserver().observeBlocks() ;
        WorldModel gridsAndBlocksStates = SeEnvironmentKt.toWorldModel(rawGridsAndBlocksStates) ;
        // HACK: make the grids and blocks marked as dynamic elements. SE sends them as non-dymanic
        // that will cause them to be ignored by mergeObservation.
        SEBlockFunctions.hackForceDynamicFlag(gridsAndBlocksStates) ;
        // assign a fresh timestamp too:
        assignTimeStamp(gridsAndBlocksStates,updateCount) ;
        for(var e : gridsAndBlocksStates.elements.entrySet()) {
            WorldEntity entity = e.getValue() ;
            if (entity.type.equals("grid") && stripOutTopLevelGrid) {
                // a top-level grid, and the option wants to strip it:
                var grid = entity ;
                for (var child : grid.elements.values()) {
                    newWom.elements.put(child.id,child) ;
                }
            }
            else {
                newWom.elements.put(e.getKey(), e.getValue()) ;
            }
        }
        // assigning a time-stamp and updating the count:
        if (useSystemTimeForTimeStamping) {
            assignTimeStamp(newWom,System.currentTimeMillis()) ;
        }
        else {
            assignTimeStamp(newWom,updateCount) ;
        }
        updateCount++ ;

        if(navgrid.origin == null) {
            // TODO .. we should also reset the grid if the agent flies to a new plane.
            navgrid.resetGrid(newWom.position);
        }
        if(worldmodel == null) {
            // this is the first observation
            worldmodel = newWom ;
        }
        else {
            // MERGING the two woms:
            worldmodel.mergeNewObservation(newWom) ;

            // HOWEVER, some blocks and grids-of-blocks may have been destroyed, hence
            // do not exist anymore. We need to remove them from state.wom. This is handled
            // below.
            // First, remove disappearing "cube-grids" (composition of blocks)
            List<String> tobeRemoved = worldmodel.elements.keySet().stream()
                    .filter(id -> ! newWom.elements.keySet().contains(id))
                    .collect(Collectors.toList());
            for(var id : tobeRemoved) worldmodel.elements.remove(id) ;
            // Then, we remove disappearing blocks (from grids that remain):
            for(var cubegridOld : worldmodel.elements.values()) {
                var cubeGridNew = newWom.elements.get(cubegridOld.id) ;
                tobeRemoved.clear();
                tobeRemoved = cubegridOld.elements.keySet().stream()
                        .filter(blockId -> ! cubeGridNew.elements.keySet().contains(blockId))
                        .collect(Collectors.toList());
                for(var blockId : tobeRemoved) cubegridOld.elements.remove(blockId) ;
            }

            // updating the "navigational-2DGrid:
            var blocksInWom =  SEBlockFunctions.getAllBlockIDs(worldmodel) ;
            List<String> toBeRemoved = navgrid.allObstacleIDs.stream()
                    .filter(id -> !blocksInWom.contains(id))
                    .collect(Collectors.toList());
            // first, removing obstacles that no longer exist:
            for(var id : toBeRemoved) {
                navgrid.removeObstacle(id);
            }
        }

        // then, there may also be new blocks ... we add them to the nav-grid:
        // TODO: this assumes doors are initially closed. Calculating blocked squares
        // for open-doors is more complicated. TODO.
        for(var block : SEBlockFunctions.getAllBlocks(gridsAndBlocksStates)) {
            navgrid.addObstacle(block);
            // check if it has an open-state (we then assume it is a door) and it is open :
            var isOpen = block.properties.get("isOpen") ;
            //if (isOpen != null) {
            //	System.out.println(">>>> door-like block: " + block.id + "," + block.properties.get("blockType")) ;
            // }
            if (isOpen != null && (Boolean) isOpen) {
                navgrid.setObstacleBlockingState(block,! (Boolean) isOpen);
            }
        }
        // updating dynamic blocking-state: (e.g. handling doors)
        // TODO!
    }

    // bunch of getters:

    public Vec3 orientationForward() {
        return (Vec3) worldmodel.elements.get(agentId).properties.get("orientationForward") ;
    }

    public WorldEntity targetBlock() {
        var targetId = worldmodel.elements.get(agentId).getStringProperty ("targetBlock") ;
        if (targetId == null) return null ;
        return SEBlockFunctions.findWorldEntity(worldmodel,targetId) ;
    }

    public Serializable val(String id, String property) {
        var e = this.worldmodel.elements.get(id) ;
        if (e == null)
            return null ;
        return e.properties.get(property) ;
    }

    /*
    public Serializable val(String property) {
        return val(this.agentId,property) ;
    }

    public Serializable before(String id, String property) {
        return this.worldmodel.before(id,property) ;
    }

    public Serializable before(String property) {
        return this.worldmodel.before(property) ;
    }
    */

    public float health() {
        return (float) worldmodel.elements.get(agentId).properties.get("health") ;
    }

    public boolean jetpackRunning() {
        return (boolean) worldmodel.elements.get(agentId).properties.get("jetpackRunning") ;

    }

    // helper functions

    static void assignTimeStamp(WorldModel wom, long time) {
        wom.timestamp = time ;
        for(var e : wom.elements.values()) {
            e.assignTimeStamp(time);
        }
    }



}
