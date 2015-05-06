package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;



public class App {
	
	private static final int EMPTY_CONTENT = -1;

	public static void main(String[] args) throws SQLException {
		
		spark.SparkBase.port(8000);
		
		spark.Spark.before((req,res) -> {
			
			String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
		    if (accessControlRequestHeaders != null) {
		        res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
		    } else {
		    	res.header("Access-Control-Allow-Headers", "*");
		    }
			
		    String allowOriginRequestHeaders = req.headers("Origin");
		    
		    if (allowOriginRequestHeaders != null) {
		        res.header("Access-Control-Allow-Origin", allowOriginRequestHeaders);
		    } else {
		    	res.header("Access-Control-Allow-Origin", "*");
		    }
		    
		    res.header("Access-Control-Request-Methods", "POST");
				
		});
		
		
		HealthDb db = new HealthDb();
		
		Gson gs = new Gson(); //GSON tool just for array to json and vice versa
		
		System.out.println("APIs Started");
		
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
				userInfo.next();
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

			if (foods.isBeforeFirst()) {
				response.remove(invalid);
				while (foods.next()) {
					HashMap<String, String> oneFood = new HashMap<String, String>();
					oneFood.put("resultId", Integer.toString(i));
					oneFood.put("foodId", foods.getString("FoodId"));
					oneFood.put("foodName", foods.getString("FoodName"));
									
					response.add(oneFood);
					i++;
				} 
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
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("userId"))) {
				return gs.toJson(response);
			}
			
			String dietId;
			if (!info.containsKey("dietId")) {
				HashMap<String, String> cond = new HashMap<String, String>();
				cond.put("UserId", info.get("userId"));
				cond.put("EndDate", null);
				ResultSet rs = db.executeQuery("Diet", cond);
				rs.next();
				dietId = rs.getString("DietId");
				System.out.println(dietId);
			} else {
				dietId = info.get("dietId");
			}
			
			ResultSet allDiets = db.executeQuery("Diet", "UserId", info.get("userId"));
			HashMap<String, String> dietList = new HashMap<String, String>();
			
			int index = 0;
			while (allDiets.next()) {
				if (!allDiets.getString("DietId").equals(dietId)) {
					dietList.put(("otherDiet" + index), allDiets.getString("DietId"));
					index++;
				} else {
					dietList.put("activeDiet", allDiets.getString("DietId"));
				}
			}
			response.add(dietList);
				
			ResultSet foodInDiet = db.executeQuery("DietItem", "DietId", dietId);
			
			int i=0;
			
			response.remove(invalid);
			if (foodInDiet.isBeforeFirst()) {	
				
				while (foodInDiet.next()) {
					HashMap<String, String> oneFood = new HashMap<String, String>();
					
					oneFood.put("resultId", Integer.toString(i));
					ResultSet oneFoodDetail = db.executeQuery("Food", "FoodId", foodInDiet.getString("FoodId"));
					oneFoodDetail.next();
					String foodName = oneFoodDetail.getString("FoodName");
					oneFood.put("foodName", foodName);
					oneFood.put("weekday", foodInDiet.getString("Weekday"));
					oneFood.put("mealType", foodInDiet.getString("MealType"));
										
					response.add(oneFood);
					i++;
				}
			} else {
				invalid.replace("status", "empty");
				response.add(0, invalid);
			}
						
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/diet/modify", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("update", "failed");	
			
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
			response.put("update", "successful");
			
			return gs.toJson(response); 
			
			//the json string will look like
			//{"foodName0":"Peach", "foodName1":"Egg"}
			
		});
		
		spark.Spark.post("/user/check", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("check", "invalid");
			
			if (req.contentLength() == EMPTY_CONTENT || !info.containsKey("userId") || !info.containsKey("userIp")) {
				return gs.toJson(response);
			}
			
			String checkUrl = "http://" + db.getServerName() + ":80/check_loggedin?ip=" + info.get("userIp") + "&header=" + URLEncoder.encode(req.headers("User-Agent"), "UTF-8");
	
			System.out.println(checkUrl);
			URL urlObj = new URL(checkUrl);
			BufferedReader br = new BufferedReader(new InputStreamReader(urlObj.openStream()));
			String id = br.readLine().substring(7, 8);
			
			System.out.println("ID:" + id);
				
			if (id.equals(info.get("userId"))) {
				ResultSet user = db.executeQuery("User", "UserId", info.get("userId"));
				user.next();
				response.clear();
				response.put("userName", user.getString("UserName"));
				response.put("userWeight", user.getString("Weight"));
			}			
					
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/user/login", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("login", "invalid");
			
					
			if ((req.contentLength() == EMPTY_CONTENT || (!info.containsKey("username") && !info.containsKey("email")) || !info.containsKey("password")) || !info.containsKey("userIp")) {
				return gs.toJson(response);
			}
			
			MessageDigest encrypter = MessageDigest.getInstance("SHA");
				
			encrypter.update(info.get("password").getBytes());
			byte[] encrepted = encrypter.digest();
			String encreptedString = new String(encrepted);
			
			String whichId = info.containsKey("username") ? "UserName" : "Email";
			
			ResultSet user = db.executeQuery("User", whichId, info.get(whichId.toLowerCase()));

			response.replace("login", "failed");
			
			if(!user.next()) {				
				return gs.toJson(response);
			}
			
			String dbpw = user.getString("Password");
					
			if (dbpw.equals(encreptedString)) {
				response.clear();
				
				String registerLogedInUrl = "http://" + db.getServerName() + ":80/login_user?ip=" + info.get("userIp") + "&header=" + URLEncoder.encode(req.headers("User-Agent"), "UTF-8") + "&user_id=" + user.getString("UserId");				
				
				System.out.println(registerLogedInUrl);
				URL urlObj = new URL(registerLogedInUrl);
				urlObj.openStream();
								
				response.put("login", "successful");				
				response.put("userId", user.getString("UserId"));				
				response.put("userName", user.getString("UserName"));
			} 
									
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/user/register", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("register", "invalid");
			
			
			if ((req.contentLength() == EMPTY_CONTENT || !info.containsKey("username") || !info.containsKey("password") || !info.containsKey("email"))) {
				return gs.toJson(response);
			}
			
			if (db.executeQuery("User", "UserName", info.get("username")).isBeforeFirst()) {
				return gs.toJson(response);
			}
			
			
			MessageDigest encrypter = MessageDigest.getInstance("SHA");
				
			encrypter.update(info.get("password").getBytes());
			byte[] encrepted = encrypter.digest();
			String encreptedString = new String(encrepted);
			info.replace("password", encreptedString);		
			
			Map<String, String> infoIgnoreCase = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
			infoIgnoreCase.putAll(info);
			
			HashMap<String, String> newUser = new HashMap<String, String>();
			Set<String> userTable = db.getTableSet().get("User").keySet();
			
			for (String field: userTable) {
				if (infoIgnoreCase.containsKey(field)) {
					newUser.put(field, infoIgnoreCase.get(field));
				}
			}
			
			db.executeInsert("User", newUser);
			
			response.clear();
			response.put("register", "successful");
						
			return gs.toJson(response); 
			
		});
				
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

	        public void run() {
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
