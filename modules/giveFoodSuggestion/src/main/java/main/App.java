package main;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class App {
	
	private static final int EMPTY_CONTENT = -1;

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		spark.Spark.setPort(8888);
		 
		spark.Spark.before((req,res)->{			
		    res.header("Access-Control-Allow-Headers", "*");
		    res.header("Access-Control-Request-Methods", "*");
		    res.header("Access-Control-Allow-Origin", "*");
		});
		
		Gson gs = new Gson(); //GSON tool just for array to json and vice versa
		
		//receiving JSON sent by client via POST request
		//expecting {"memberId":"10000", "foodName0":"Sandwich", "foodName1":"Butter", ...}
		spark.Spark.post("/add", "application/json", (req, res) -> {
						
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> diet = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			if (req.contentLength() == EMPTY_CONTENT) {
				diet = new HashMap<String, String>();						
			}
			
			Food[] fs = new Food[diet.size()]; //the response array to be transformed into JSON
			
			int count = 0; //loop through the food names and write them to response array
			for (String f: diet.values()) {
				fs[count] = new Food(f);
				count++;
			}
			
			res.type("application/json"); //define return type
			return gs.toJson(fs); //use GSON to transform the array to JSON
			
			//the json string will look like
			//{"name":"saobi","calorie":0.0,"secondaryNutritionInfo":{"vitamin G":123123.0,"hydrogen":123.0}}
			
		});
		
		spark.Spark.post("/bmi", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("bmi", "0");
			response.put("status", "invalid");
			
			if ((req.contentLength() == EMPTY_CONTENT) || !info.containsKey("hasRegistered")) {
				return gs.toJson(response);
			}
			
			if (info.get("hasRegistered").equals("false")) {
				response = ModuleMaths.computeBmi(Double.valueOf(info.get("height")), Double.valueOf(info.get("weight")));
			} else if (info.get("hasResistered").equals("true")) {
				
			}
						
			return gs.toJson(response); //use GSON to transform the array to JSON
			
			//the json string will look like
			//{"bmi":"100","status":"obese"}
			
		});
		

	}

}
