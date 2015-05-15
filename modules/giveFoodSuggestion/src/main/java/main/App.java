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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;



public class App {
	
	private static final int EMPTY_CONTENT = -1;
	
	private static final HashMap<String, ArrayList<String>> compulsoryFields = new HashMap<String, ArrayList<String>>();

	public static void main(String[] args) throws SQLException {
		
		compulsoryFields.put("/bmi", new ArrayList<String>(Arrays.asList(new String[] {
				"hasRegistered"
				})));
		
		compulsoryFields.put("/food", new ArrayList<String>(Arrays.asList(new String[] {
				"searchString"
				})));
		
		compulsoryFields.put("/diet", new ArrayList<String>(Arrays.asList(new String[] {
				"userId"
				})));
		
		compulsoryFields.put("/diet/modify", new ArrayList<String>(Arrays.asList(new String[] {
				"userId",
				"foodId",
				"dietId",
				"weekday",
				"mealType",
				"modiType"
				})));
		
		compulsoryFields.put("/diet/create", new ArrayList<String>(Arrays.asList(new String[] {
				"userId",
				"dietName",
				"dietType",
				})));
		
		compulsoryFields.put("/diet/delete", new ArrayList<String>(Arrays.asList(new String[] {
				"userId",
				"dietId",
				"userIp"
				})));
		
		compulsoryFields.put("/user/check", new ArrayList<String>(Arrays.asList(new String[] {
				"userIp"
				})));
		
		compulsoryFields.put("/user/login", new ArrayList<String>(Arrays.asList(new String[] {
				"password",
				"userIp"
				})));
		
		compulsoryFields.put("/user/register", new ArrayList<String>(Arrays.asList(new String[] {
				"username",
				"password",
				"email"
				})));
		
		
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
					
					HashMap<String, String> foodTable = db.getTableSet().get("Food");
					Set<String> ks = foodTable.keySet();
					
					for (String field: ks) {
						
						String fieldClone = field;
						
						if (field.equals("FoodName") || field.equals("FoodId")) {
							field = field.toLowerCase().charAt(0) + field.substring(1);					
						}
						
						oneFood.put(field, foods.getString(fieldClone));
						
					}
									
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
			
			if (!isReqValid("/diet", info.keySet())) {
				return gs.toJson(response);
			}
			
			String dietId = null;
			ResultSet allDiets = db.executeQuery("Diet", "UserId", info.get("userId"));
			
			while (allDiets.next()) {
				HashMap<String, String> oneDiet = new HashMap<String, String>();
				Set<String> fields = db.getTableSet().get("Diet").keySet();
				
				String type;
				if (!info.containsKey("dietId")) {
					if (allDiets.getDate("EndDate") != null) {
						type = "other";
					} else {
						dietId = allDiets.getString("dietId");
						type = "active";
					}
				} else {
					dietId = info.get("dietId");
					if (!allDiets.getString("DietId").equals(dietId)) {
						type = (allDiets.getDate("EndDate") == null ? "current" : "other");
					} else {
						type = "active";
					}
				}
				
				oneDiet.put("activeType", type);
				
				for (String field: fields) {
					oneDiet.put(field.toLowerCase().charAt(0) + field.substring(1), allDiets.getString(field));
				}
				
				response.add(oneDiet);
				
			}
			
			response.remove(invalid);
			
			try {
				ResultSet foodInDiet = db.executeQuery("DietItem", "DietId", dietId);
				if(!foodInDiet.isBeforeFirst()) {
					throw new Exception("Empty diet");
				}
							
				while (foodInDiet.next()) {
					HashMap<String, String> oneFood = new HashMap<String, String>();
					
					ResultSet oneFoodDetail = db.executeQuery("Food", "FoodId", foodInDiet.getString("FoodId"));
					oneFoodDetail.next();
					
					HashMap<String, String> foodTable = db.getTableSet().get("Food");
					
					for (String field: foodTable.keySet()) {
						
						String fieldClone = field;
						
						if (field.equals("FoodName") || field.equals("FoodId")) {
							fieldClone = fieldClone.toLowerCase().charAt(0) + fieldClone.substring(1);					
						}

						oneFood.put(fieldClone, oneFoodDetail.getString(field));
						
					}
					
					oneFood.put("weekday", foodInDiet.getString("Weekday"));
					oneFood.put("mealType", foodInDiet.getString("MealType"));
										
					response.add(oneFood);
					
				}
				
			} catch (NullPointerException|NumberFormatException e) {
				
				invalid.replace("status", "noDiet");
				response.add(invalid);
				e.printStackTrace();
				
			} catch (Exception e) {
				
				invalid.replace("status", "empty");
				response.add(invalid);
				e.printStackTrace();
				
			}
			
						
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/diet/modify", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("update", "failed");	
			
			if (!isReqValid("/diet/modify", info.keySet())) {
				return gs.toJson(response);
			}
			
			Set<String> ks = db.getTableSet().get("DietItem").keySet();
			HashMap<String, String> foodInDiet = new HashMap<String,String>();
			
			for (String field: ks) {
				foodInDiet.put(field, info.get(field.toLowerCase().charAt(0) + field.substring(1)));
			}
			
			try {
				if(info.get("modiType").equals("delete")) {
					db.executeDelete("DietItem", foodInDiet);
				} else if(info.get("modiType").equals("add")) {
					db.executeInsert("DietItem", foodInDiet);
				} else if(info.get("modiType").equals("start")) {
					HashMap<String, String> cond = new HashMap<String, String>();
					cond.put("EndDate", null);
					cond.put("UserId", info.get("userId"));
					
					HashMap<String, String> newValue = new HashMap<String, String>();
					newValue.put("EndDate", "now()");
					
					db.executeUpdate("Diet", cond, newValue);
					
					cond.clear();
					cond.put("DietId", info.get("dietId"));					
					newValue.put("StartDate", "now()");
					newValue.replace("EndDate", null);		
					
					db.executeUpdate("Diet", cond, newValue);
				} else {
					throw new Exception("modiType is not valid");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return gs.toJson(response);
			}
			
			response.remove(0);
			response.put("update", "successful");
			
			return gs.toJson(response); 
			
			//the json string will look like
			//{"foodName0":"Peach", "foodName1":"Egg"}
			
		});
		
		spark.Spark.post("/diet/create", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("dietCreate", "invalid");
			
			if (!isReqValid("/diet/create", info.keySet())) {
				return gs.toJson(response);
			}
			
			try {
				HashMap<String, String> cond = new HashMap<String, String>();
				HashMap<String, String> val = new HashMap<String, String>();
				
				cond.put("EndDate", null);
				cond.put("UserId", info.get("userId"));
				val.put("EndDate", "now()");
				
				db.executeUpdate("Diet", cond, val);
				
				@SuppressWarnings("unchecked")
				HashMap<String, String> infoClone = (HashMap<String, String>) info.clone();
				
				for (String field: infoClone.keySet()) {
					info.put(field.toUpperCase().charAt(0) + field.substring(1), info.get(field));
					info.remove(field);
				}
				
				
				int id = db.executeInsert("Diet", info);
				
				response.put("dietId", Integer.toString(id));
				response.replace("dietCreate", "successful");
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/diet/delete", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("dietDelete", "invalid");
			
			if (!isReqValid("/diet/delete", info.keySet())) {
				return gs.toJson(response);
			}
			
			String checkUrl = "http://" + db.getServerName() + ":80/check_loggedin?ip=" + info.get("userIp") + "&header=" + URLEncoder.encode(req.headers("User-Agent"), "UTF-8");
			
			URL urlObj = new URL(checkUrl);
			BufferedReader br = new BufferedReader(new InputStreamReader(urlObj.openStream()));
			String json = br.readLine();
			
			try {
				String idValue = json.substring(7, json.indexOf("\"}"));
				
				if(!idValue.equals(info.get("userId"))) {
					throw new Exception("Not current user");
				}
				
				info.remove("userIp");
				
				@SuppressWarnings("unchecked")
				HashMap<String, String> infoClone = (HashMap<String, String>) info.clone();
				
				for (String field: infoClone.keySet()) {
					info.put(field.toUpperCase().charAt(0) + field.substring(1), info.get(field));
					info.remove(field);
				}
				
				infoClone.clear();
				infoClone.put("DietId", info.get("DietId"));
				
				HashMap<String, String> cond = new HashMap<String, String>();
				HashMap<String, String> val = new HashMap<String, String>();
				
				cond.put("EndDate", null);
				cond.put("UserId", info.get("UserId"));
				
				ResultSet rs = db.executeQuery("Diet", cond);
				
				if(rs.next() && rs.getString("DietId").equals(info.get("DietId"))) {
					response.replace("dietDelete", "currentDiet");
					throw new Exception("Can't delete current active diet");
				}
				
				db.executeDelete("DietItem", infoClone);
				db.executeDelete("Diet", info);
				response.replace("dietDelete", "successful");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/user/check", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("check", "invalid");
			
			if (!isReqValid("/user/check", info.keySet())) {
				return gs.toJson(response);
			}
			
			String checkUrl = "http://" + db.getServerName() + ":80/check_loggedin?ip=" + info.get("userIp") + "&header=" + URLEncoder.encode(req.headers("User-Agent"), "UTF-8");
			
			URL urlObj = new URL(checkUrl);
			BufferedReader br = new BufferedReader(new InputStreamReader(urlObj.openStream()));
			String json = br.readLine();
			System.out.println(checkUrl);
			
			try {
				String idValue = json.substring(7, json.indexOf("\"}"));
				
				int id = Integer.parseInt(idValue);
				ResultSet user = db.executeQuery("User", "UserId", Integer.toString(id));
				user.next();
				response.clear();
				
				Set<String> ks = db.getTableSet().get("User").keySet();
				for (String field: ks) {
					if (!field.equals("Password")) {
						response.put(field.toLowerCase().charAt(0) + field.substring(1), user.getString(field));
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
					
			return gs.toJson(response); 
			
		});
		
		spark.Spark.post("/user/login", "application/json", (req, res) -> {
			
			Type hashMap = new TypeToken<HashMap<String, String>>(){}.getType();
			HashMap<String, String> info = gs.fromJson(req.body(), hashMap); //JSON to ArrayList
			
			res.type("application/json"); //define return type
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("login", "invalid");
			
					
			if (!isReqValid("/user/login", info.keySet()) && (!info.containsKey("username") && !info.containsKey("email"))) {
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
	
	private static boolean isReqValid(String api, Set<String> ks) {
		
		for (String field: compulsoryFields.get(api)) {
			if (!ks.contains(field)) {
				return false;
			}
		}
		
		return true;
	}

}
