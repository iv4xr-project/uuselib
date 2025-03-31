package nl.uu.cs.uuspaceagent.construction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.InputStream;

import org.json.JSONObject;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import nl.uu.cs.uuspaceagent.DPos3;
import spaceEngineers.model.DefinitionId;

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
	
	static void addRecord(String structure, ConstructionOptimizer optimizer, Float time, int turns){
		// Load records file
		JSONObject jsonObject = loadRecords("records.json");
		
		// Format structureName
		structure = structure.substring(structure.lastIndexOf('/')+1, structure.length());
		
		// Format optimizerName
		String optimizerName = "Default";
		if (optimizer != null)
		{
			optimizerName = optimizer.getClass().getName();
			optimizerName = optimizerName.substring(optimizerName.lastIndexOf('.')+1, optimizerName.length());
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
				new JSONObject().put("time", time).put("turns", turns)	
				);
		
		saveRecords(jsonObject, "records.json");
	}
}
