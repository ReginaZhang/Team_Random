package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


public class App {
	
	private static final int EMPTY_CONTENT = -1;

	public static void main(String[] args) throws SQLException {
		
		spark.SparkBase.port(8000);
		
		spark.Spark.options("/*", (req,res)->{
			 
		    String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
		    if (accessControlRequestHeaders != null) {
		        	res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
		    	} else {
		    		res.header("Access-Control-Request-Headers", "Content-Type, content-type");
		    	}
		    
		    res.header("Access-Control-Request-Methods", "POST");
		    res.header("Access-Control-Allow-Origin", "http://45.56.85.191:8080");
		    
		    return "OK";
		});
		
		HealthDb db = new HealthDb();
		
		Gson gs = new Gson(); //GSON tool just for array to json and vice versa
		
		System.out.println("Reached server roots");
		
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
				int id = Integer.valueOf(info.get("userId"));
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
					oneFood.put("foodId", foods.getString("FoodId"));
					oneFood.put("foodName", foods.getString("FoodName"));
									
					response.add(oneFood);
					i++;
				} while (foods.next());
			}
						
			return gs.toJson(response); 
			
			
			//the json string will look like
			//{"foodName0":"Peach", "foodName1":"Egg"}
			
		});
		
		spark.Spark.post("/diet", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			ArrayList<HashMap<String, String>> response = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> invalid = new HashMap<String, String>();
			invalid.put("status", "invalid");
			response.add(invalid);			
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("dietId"))) {
				return gs.toJson(response);
			}
			
			ResultSet foodInDiet = db.executeQuery("DietItem", "DietId", info.get("dietId").trim());
			
			int i=0;

			if (foodInDiet.getRow() != 0) {
				response.remove(invalid);
				do {
					HashMap<String, String> oneFood = new HashMap<String, String>();
					
					oneFood.put("resultId", Integer.toString(i));				
					String foodName = db.executeQuery("Food", "FoodId", foodInDiet.getString("FoodId")).getString("FoodName");
					oneFood.put("foodName", foodName);				
					
					response.add(oneFood);
					i++;
				} while (foodInDiet.next());
			}
						
			return gs.toJson(response); 
			
			
			//the json string will look like
			//{"foodName0":"Peach", "foodName1":"Egg"}
			
		});
		
		spark.Spark.post("/diet/modify", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			ArrayList<HashMap<String, String>> response = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> invalid = new HashMap<String, String>();
			invalid.put("update", "failed");
			response.add(invalid);			
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("foodId") || !info.containsKey("dietId") ||
					!info.containsKey("modiType"))) {
				return gs.toJson(response);
			}
			
			HashMap<String, String> foodInDiet = new HashMap<String,String>();
			foodInDiet.put("FoodId", info.get("foodId"));
			foodInDiet.put("DietId", info.get("dietId"));
			
			if(info.get("modiType").equals("delete")) {
				db.executeDelete("DietItem", foodInDiet);
			} else if(info.get("modiType").equals("add")) {
				db.executeInsert("DietItem", foodInDiet);
			} else {
				return gs.toJson(response);
			}
			
			response.remove(0);
			HashMap<String, String> status = new HashMap<String, String>();
			status.put("update", "successful");
			response.add(status);
			
			return gs.toJson(response); 
			
			//the json string will look like
			//{"foodName0":"Peach", "foodName1":"Egg"}
			
		});
		
		spark.Spark.post("/user/login", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			ArrayList<HashMap<String, String>> response = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> invalid = new HashMap<String, String>();
			invalid.put("login", "invalid");
			response.add(invalid);		
			
			
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("username") || !info.containsKey("password"))) {
				return gs.toJson(response);
			}
			
			MessageDigest encrypter = MessageDigest.getInstance("SHA");
				
			encrypter.update(info.get("password").getBytes());
			byte[] encrepted = encrypter.digest();
			String encreptedString = new String(encrepted);
			
			String dbpw = db.executeQuery("User", "UserName", info.get("username")).getString("Password");
			
			response.remove(0);
			HashMap<String, String> status = new HashMap<String, String>();
			
			if (dbpw.equals(encreptedString)) {			
				status.put("login", "successful");
			} else {
				status.put("login", "failed");
			}
			
			response.add(status);
						
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/user/register", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			ArrayList<HashMap<String, String>> response = new ArrayList<HashMap<String, String>>();
			
			HashMap<String, String> invalid = new HashMap<String, String>();
			invalid.put("register", "invalid");
			response.add(invalid);		
			
			
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("username") || !info.containsKey("password") || !info.containsKey("email"))) {
				return gs.toJson(response);
			}
			
			MessageDigest encrypter = MessageDigest.getInstance("SHA");
				
			encrypter.update(info.get("password").getBytes());
			byte[] encrepted = encrypter.digest();
			String encreptedString = new String(encrepted);
			
			HashMap<String, String> newUser = new HashMap<String, String>();
			newUser.put("UserName", info.get("username"));
			newUser.put("Password", encreptedString);
			newUser.put("Email", info.get("email"));
			
			db.executeInsert("User", newUser);
			
			response.remove(0);
			HashMap<String, String> status = new HashMap<String, String>();		
			status.put("register", "successful");
			response.add(status);
						
			return gs.toJson(response); 
			
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
