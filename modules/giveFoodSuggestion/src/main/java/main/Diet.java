package main;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

public class Diet {
	
	public enum DietType {
		
		LOSE_WEIGHT ("L"), 
		FITNESS ("F"), 
		GENERAL("G");
	
		private final String value;
		private DietType(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	
	};
	
	private int dietId;
	private String dietName;
	private DietType dietType;
	private int userId;
	private ArrayList<Food> foodList = new ArrayList<Food>();
	private Date startDate;
	private Date endDate;
	
	public Diet(int dietId, String dietName, DietType dietType, int userId,
			ArrayList<Food> foodList, Date startDate, Date endDate) {
		super();
		this.dietId = dietId;
		this.dietName = dietName;
		this.dietType = dietType;
		this.userId = userId;
		this.foodList = foodList;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public void addFoodToDiet(Food f, HealthDb db) {
		
		HashMap<String, String> foodDiet = new HashMap<String, String>();
		foodDiet.put("FoodId", Integer.toString(f.getFoodId()));
		foodDiet.put("DietId", Integer.toString(this.dietId));

		db.executeInsert("DietItem", foodDiet);
		
	}
	
	public void deleteFoodFromDiet(Food f, HealthDb db) {
		
		HashMap<String, String> foodDiet = new HashMap<String, String>();
		foodDiet.put("FoodId", Integer.toString(f.getFoodId()));
		foodDiet.put("DietId", Integer.toString(this.dietId));
		
		db.executeDelete("DietItem", foodDiet);
		
	}
	
	public void writeDietToDb(HealthDb db) {
		
		HashMap<String, String> thisDiet = new HashMap<String, String>();
		thisDiet.put("DietId", Integer.toString(dietId));
		thisDiet.put("DietName", dietName);
		thisDiet.put("DietType", dietType.value);
		thisDiet.put("UserId", Integer.toString(userId));
		thisDiet.put("StartDate", startDate.toString());
		thisDiet.put("EndDate", endDate.toString());
		
		db.executeInsert("Diet", thisDiet);
		
		HashMap<String, String> foodInDiet = new HashMap<String, String>();
		for(Food f: foodList) {
			foodInDiet.put("FoodId", Integer.toString(f.getFoodId()));
			foodInDiet.put("DietId", Integer.toString(this.dietId));
			
			db.executeInsert("DietItem", foodInDiet);
			foodInDiet.clear();
		}
		
		
	}
	

}
