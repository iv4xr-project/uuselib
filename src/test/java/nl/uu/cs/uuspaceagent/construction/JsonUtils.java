package nl.uu.cs.uuspaceagent.construction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStream;

import org.json.JSONObject;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import nl.uu.cs.uuspaceagent.DPos3;
import spaceEngineers.model.DefinitionId;

import static nl.uu.cs.aplib.utils.CSVUtility.exportToCSVfile;
import static nl.uu.cs.uuspaceagent.TestUtils.console;

public class JsonUtils {
	static JSONObject loadRecords(String fileName) {
		JSONObject jsonObject = null;
		try {
			File f = new File(fileName);
			
			String content = new String(Files.readAllBytes(Paths.get(fileName)));
			jsonObject = new JSONObject(content);
			
		} catch (JsonIOException | JsonSyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			console("No file found, creating blank object");
			jsonObject = new JSONObject();
		} 
		
		return jsonObject;
	}
	
	static boolean saveRecords(JSONObject jsonObject, String fileName) {
		
		try {
			PrintWriter out = new PrintWriter(fileName);
			out.print(jsonObject);
			out.close();
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return false;
	}
	
	static void addRecord(String structure, ConstructionOptimizer optimizer, Float time, int turns, int priorityCases, boolean survival){
		// Load records file
		JSONObject jsonObject = loadRecords("results/records.json");
		
		if (survival)
			structure += "-survival";
		
		// Format optimizerName
		String optimizerName = "Default";
		if (optimizer != null)
		{
			optimizerName = optimizer.getName();
		}
				
		// Create structureObject if specified structure doesn't have any records yet.
		if (!jsonObject.has(structure)) {
			var structureObject = new JSONObject();
			jsonObject.put(structure, structureObject);
		}
		
		// Get structureObject and add record
		JSONObject structureObject = (JSONObject) jsonObject.get(structure);
		structureObject.put(
				optimizerName, 
				new JSONObject().put("time", time).put("turns", turns).put("priorityCases", priorityCases)	
				);
		
		saveRecords(jsonObject, "results/records.json");
	}
	
	static void exportCSV(String structure, ConstructionOptimizer optimizer, ArrayList<Number[]> agentPositions, boolean survival) {
		String optimizerName = "Default";
 		if (optimizer != null)
 		{
 			optimizerName = optimizer.getName();
 		}		
        var columnNames = new String[]{"X", "Y", "Z"};
        
        var structureName = structure + (survival ? "-survival" : "");
        
        var fileName = "results/" + structureName + "-" + optimizerName + ".csv";
        try {
			exportToCSVfile(Character.valueOf(','), columnNames, agentPositions, fileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
