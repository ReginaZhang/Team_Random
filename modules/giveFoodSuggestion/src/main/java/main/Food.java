package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/** 
 * @author ra
 * Please note that a negative value stored represents an invalid value has been given. 
 */
public class Food {
	
	public static final String[] MAJOR_NUTRIS_NAMES = {"Energy", "Protein", "Total Fat",
		"Saturated Fat", "Total Carbohydrate", "Sugars", "Sodium"};
	
	private final int FoodId;
	private final String name;
	private final String type;
	
	private final HashMap<String, Double> majorNutritionInfo;
	private final HashMap<String, Double> secondaryNutritionInfo;
	
	public Food (int id, String n, String t, HashMap<String, Double> mni, HashMap<String, Double> sni) {
		this.FoodId = id;
		this.name = n;
		this.type = t;
		
		if (isMniInputValid(mni)) {
			this.majorNutritionInfo = mni;
		}
		else {
			this.majorNutritionInfo = new HashMap<String, Double>();
			this.majorNutritionInfo.put("Invalid", -1.0);
			System.out.println("Invalid mni given to constructor");
		}
		
		this.secondaryNutritionInfo = sni;
	}

	//just for testing purposes!
	public Food (String n) {
		this.FoodId = -1;
		this.name = n;
		this.type = null;
		
		this.majorNutritionInfo = new HashMap<String, Double>();
		this.majorNutritionInfo.put("Invalid", -1.0);
		
		this.secondaryNutritionInfo = new HashMap<String, Double>();
		this.secondaryNutritionInfo.put("Invalid", -1.0);
	}

	public boolean isSuitable (ArrayList<Food> diet, HashMap<String, Double> limit) {
		
		HashMap<String, Double> sum = this.majorNutritionInfo;
		
		if (isMniInputValid(limit)) {
			for (String nutriName: Food.MAJOR_NUTRIS_NAMES) {
				sum.put(nutriName, sum.get(nutriName) + limit.get(nutriName));
			}
		} 
		else {
			System.out.println("Invalid limit given");
			return false;
		}
			
		for (String nutriName: Food.MAJOR_NUTRIS_NAMES) {
			if (sum.get(nutriName) > limit.get(nutriName)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static String[] getMajorNutrisNames() {
		return MAJOR_NUTRIS_NAMES;
	}
	
	public int getFoodId() {
		return FoodId;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public HashMap<String, Double> getMajorNutritionInfo() {
		return majorNutritionInfo;
	}

	public HashMap<String, Double> getSecondaryNutritionInfo() {
		return secondaryNutritionInfo;
	}

	private boolean isMniInputValid (HashMap<String, Double> mni) {
		
		if (mni.size() != 7) {
			return false;
		}
		
		for (String name: mni.keySet()) {
			//if (!Arrays.asList(MAJOR_NUTRIS_NAMES).contains(name)) { 
			if (Arrays.asList(MAJOR_NUTRIS_NAMES).contains(name)) {  //testing, currently only energy is recorded in db
				//return false;
				return true;
			}
		}
		return true;
	}

}
