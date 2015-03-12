package testgroup;

import static spark.Spark.*;
import com.google.gson.Gson;

/**
 * Hello world!
 *
 */
public class App{     
    public static void main( String[] args ){
	Gson gson = new Gson(); //This Gson object is used for conversion to/from JSON
	
        get("/test", (req, res) -> "Hello World");

	get("/gimme_json/:food", (req, res) -> {
		String food = req.params(":food"); //Get the food parameter
		NutritionInfo info = new NutritionInfo(food); //Make a NutritionInfo object
		res.type("application/json"); //Set the response type to JSON
		return gson.toJson(info); //Return a JSON representation of the NutritionInfo object
	    });
    }
}


class NutritionInfo{
    final String whatFood;
    NutritionInfo(String whatFood){
	this.whatFood = whatFood;
    }
}
