package main;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class App {

	public static void main(String[] args) {
		
		Gson gs = new Gson(); //GSON tool just for array to json and vice versa
		
		//receiving JSON sent by client via GET request
		spark.Spark.get("/ask_suggestion/", "application/json", (req, res) -> {
						
			Type arrayListType = new TypeToken<ArrayList<String>>(){}.getType();
			ArrayList<String> diet = gs.fromJson(req.body(), arrayListType); //JSON to ArrayList, assuming JSON is an array of food names
			
			Food[] fs = new Food[diet.size()]; //the response array to be transformed into JSON
			
			int count = 0; //loop through the food names and write them to response array
			for (String f: diet) {
				fs[count] = new Food(f);
				count++;
			}
			
			res.type("application/json"); //define return type
			return gs.toJson(fs); //use GSON to transform the array to JSON
			
		});
		
		spark.Spark.options("/*", (request,response)->{
			 
		    String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
		    if (accessControlRequestHeaders != null) {
		        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
		    }
		 
		    String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
		    if(accessControlRequestMethod != null){
		    response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
		    }
		 
		    return "OK";
		});
		 
		spark.Spark.before((request,response)->{
		    response.header("Access-Control-Allow-Origin", "*");
		});

	}

}
