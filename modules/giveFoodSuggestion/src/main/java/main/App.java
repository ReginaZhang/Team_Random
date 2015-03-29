package main;

import static spark.Spark.*;
import static spark.SparkBase.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class App {
	
	private static final int EMPTY_CONTENT = -1;

	public static void main(String[] args) throws SQLException {
		
		spark.SparkBase.port(8888);
		 
		spark.Spark.before((req,res)->{			
		    res.header("Access-Control-Allow-Headers", "*");
		    res.header("Access-Control-Request-Methods", "*");
		    res.header("Access-Control-Allow-Origin", "*");
		});
		
		HealthDb db = new HealthDb();
		
		
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
				response = computeBmi(Double.valueOf(info.get("height")), Double.valueOf(info.get("weight")));
			} else if (info.get("hasRegistered").equals("true")) {
				int id = Integer.valueOf(info.get("memberId"));
				ResultSet userInfo = db.executeQuery("User", "UserId", Integer.toString(id));
				response = computeBmi(userInfo.getDouble("Height"), userInfo.getDouble("Weight"));
			}
						
			return gs.toJson(response); 
			
			//the json string will look like
			//{"bmi":"100","status":"obese"}
			
		});
		
		spark.Spark.post("/food", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			ArrayList<HashMap<String, String>> response = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> invalid = new HashMap<String, String>();
			invalid.put("food", "invalid");
			response.add(invalid);			
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("searchString"))) {
				return gs.toJson(response);
			}
			
			ResultSet foods = db.executeQuery("Food", "FoodName", info.get("searchString").trim());
			
			int i=0;

			if (foods.getRow() != 0) {
				response.remove(invalid);
				do {
					HashMap<String, String> oneFood = new HashMap<String, String>();
					oneFood.put("resultId", Integer.toString(i));
					oneFood.put("foodName", foods.getString("FoodName"));
					
					
					response.add(oneFood);
					i++;
				} while (foods.next());
			}
						
			return gs.toJson(response); 
			
			
			//the json string will look like
			//{"foodName0":"Peach", "foodName1":"Egg"}
			
		});
		
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

	        public void run() {
	            try {
					db.getConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	            spark.SparkBase.stop();
	        }
	        
	    }));
		

	}
	
	private static HashMap<String, String> computeBmi(double height, double weight) {
		
		double bmi = weight/(height*height);
		String status = bmi < 18.5 ? "underweight" : (bmi <= 24.9 ? "normal" : (bmi <= 29.9 ? "overweight" : "obese"));
				
		HashMap<String, String> bmiInfo = new HashMap<String, String>();
		bmiInfo.put("bmi", Double.toString(bmi));
		bmiInfo.put("status", status);
		return  bmiInfo;
		
	}

}
