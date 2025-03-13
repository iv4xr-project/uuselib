package nl.uu.cs.uuspaceagent;

import static nl.uu.cs.uuspaceagent.TestUtils.console;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nl.uu.cs.aplib.utils.Pair;
import spaceEngineers.model.DefinitionId;

public class Blueprint {
	
	DefinitionId[][][] definitionIds;
	WorldEntity[][][] entities;
	public DPos3 originCell;
	
	public Blueprint(DefinitionId[][][] definitionIds, DPos3 originCell) {
		this.definitionIds = definitionIds;
		this.originCell = originCell;
		var size = getSize();
		this.entities = new WorldEntity[size.x][size.y][size.z];
	}
	
	public DPos3 getSize() {
		return new DPos3(definitionIds.length, definitionIds[0].length, definitionIds[0][0].length);
	}
	
	public int getAABBVolume() {
		var size = getSize();
		return size.x * size.y * size.z;
	}
	
	public DefinitionId getDefinitionAtCell(DPos3 cellPosition) {
		if (!isWithinBounds(cellPosition))
			return null;
		var blockDefinition = definitionIds[cellPosition.x][cellPosition.y][cellPosition.z];
		return blockDefinition;
	}
	
	public WorldEntity getEntityAtCell(DPos3 cellPosition) {
		if (!isWithinBounds(cellPosition))
			return null;
		var entity = entities[cellPosition.x][cellPosition.y][cellPosition.z];
		return entity;
	}
	
	boolean isWithinBounds(DPos3 cellPosition) {
		if (cellPosition.x < 0 || cellPosition.y < 0 || cellPosition.z < 0 ||
				cellPosition.x >= definitionIds.length || cellPosition.y >= definitionIds[0].length || cellPosition.z >= definitionIds[0][0].length)
			return false;
		return true;
	}
	
	public boolean addEntity(DPos3 cellPosition, WorldEntity entity) {
		var currentValueOfCell = getEntityAtCell(cellPosition);
		if (currentValueOfCell != null)
		{
			console("Can't insert entity " + entity.id + " at cell " + cellPosition.toString() + 
					" because it already contains " + currentValueOfCell.id);
			return false;
		}
		
		entities[cellPosition.x][cellPosition.y][cellPosition.z] = entity;
		return true;
	}
	
	public boolean addDefinition(DPos3 cellPosition, DefinitionId definition) {
		
		definitionIds[cellPosition.x][cellPosition.y][cellPosition.z] = definition;
		return true;
	}
	
	public List<DPos3> getNeighborPositions(DPos3 cellPosition){
		ArrayList<DPos3> neighbors = new ArrayList<DPos3>();
		var p = cellPosition;
		for (int x = p.x-1 ; x <= p.x+1 ; x++) 
			for (int y = p.y-1 ; y <= p.y+1 ; y++) 
				for (int z = p.z-1 ; z <= p.z+1 ; z++) {
					if(x==p.x && y==p.y && z==p.z) continue;
					DPos3 neighbor = new DPos3(x, y, z);
					if(isWithinBounds(neighbor)) neighbors.add(neighbor);
		        }
		return neighbors;
	}
	
	public List<WorldEntity> getNeighbourEntities(DPos3 cellPosition){
		
		var neighbours = getNeighborPositions(cellPosition).stream().filter((DPos3 pos) -> (getEntityAtCell(pos) != null))
				.map((DPos3 pos) -> (getEntityAtCell(pos))).toList();
		return neighbours;
	}
	
	public List<DefinitionId> getNeighbourDefinitions(DPos3 cellPosition){
		
		var neighbours = getNeighborPositions(cellPosition).stream().filter((DPos3 pos) -> (getDefinitionAtCell(pos) != null))
				.map((DPos3 pos) -> (getDefinitionAtCell(pos))).toList();
		return neighbours;
	}
	
	public List<DPos3> getUnplacedCells(){
		var size = getSize();
		var unplacedCells = new ArrayList<DPos3>();
		
		for (int x = 0 ; x < size.x ; x++) 
			for (int y = 0 ; y < size.y ; y++) 
				for (int z = 0 ; z < size.z ; z++) {
					DPos3 cellPosition = new DPos3(x, y, z);
					if (getDefinitionAtCell(cellPosition) == null) continue;
					if (getEntityAtCell(cellPosition) != null) continue;
					
					unplacedCells.add(cellPosition);
		}
		
		return unplacedCells;
	}

	public boolean checkValidity() {
		var size = getSize();
		for (int x = 0 ; x < size.x ; x++) 
			for (int y = 0 ; y < size.y ; y++) 
				for (int z = 0 ; z < size.z ; z++) {
					DPos3 cellPosition = new DPos3(x, y, z);
					
					// Block placed in a cell that should remain empty.
                    if (getEntityAtCell(cellPosition) != null && getDefinitionAtCell(cellPosition) == null)
                    	return false;
                    
                    // Floating block with no neighbors
                    if (getDefinitionAtCell(cellPosition) != null && getAABBVolume() > 1) {
                    	var neighborPositions = getNeighborPositions(cellPosition);
                    	int neighborBlockCount = (int) neighborPositions.stream().filter((DPos3 pos) -> (getDefinitionAtCell(pos) != null)).count();
                    	if (neighborBlockCount == 0) return false;
                    }
		        }
		return true;
	}
	
	/**
	 * @return A value between 0 and 1 indicating how much of the construction has been completed.
	 */
	public float getProgress() {
		int blocksToPlace = 0;
		int blocksPlaced = 0;
		
		var size = getSize();
		for (int x = 0 ; x < size.x ; x++) 
			for (int y = 0 ; y < size.y ; y++) 
				for (int z = 0 ; z < size.z ; z++) {
					DPos3 cellPosition = new DPos3(x, y, z);
					blocksToPlace += getDefinitionAtCell(cellPosition)!=null ? 1 : 0;
					blocksPlaced += getEntityAtCell(cellPosition)!=null ? 1 : 0;
				}
		return blocksPlaced / (float)blocksToPlace;
	}
	
	public static Blueprint loadFromFile(String fileName)
	{
		Blueprint blueprint = null;
		class Local {
			public List<String> stringToValues(String input) {
				var output = Arrays.asList(input.split(";"));
				return output;
			}
			
			public DPos3 valuesToDPos3(String coordinates) {
				var components = Arrays.asList(coordinates.substring(1, coordinates.length()-1).split(","))
						.stream().map((String component) -> Integer.parseInt(component))
						.toList();
				return new DPos3(components.get(0), components.get(1), components.get(2));
			}
			
			public DPos3 parseCoordinateLine(String line) {
				var values = stringToValues(line);
				DPos3 pos = valuesToDPos3(values.get(0));
				return pos;
			}
			
			public Pair<DPos3, DefinitionId> parseBlockLine(String line){
				var values = stringToValues(line);
				DPos3 pos = valuesToDPos3(values.get(0));
				var def = DefinitionId.Companion.create(values.get(2), values.get(1));
				
				return new Pair<>(pos, def);
			}
		}
		var local = new Local();
		try {
			var reader = new BufferedReader(new FileReader(fileName));
			
			// Read header values
			var line = reader.readLine();
			DPos3 size = local.parseCoordinateLine(line);
			line = reader.readLine();
			DPos3 origin = local.parseCoordinateLine(line);
			
			blueprint = new Blueprint(new DefinitionId[size.x][size.y][size.z], origin);
			
			// Read first line of blocks
			line = reader.readLine();
			
			// Parse all blocks until end of file
			while (line != null) {
				Pair<DPos3, DefinitionId> posAndDef = local.parseBlockLine(line);
				blueprint.addDefinition(posAndDef.fst, posAndDef.snd);
				
				line = reader.readLine();
			}
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return blueprint;	
	}
}