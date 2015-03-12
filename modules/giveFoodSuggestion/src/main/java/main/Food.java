package main;

import java.util.ArrayList;

public class Food {
	
	private final String name;
	private final String type;
	
	private final double calorie;
	
	public Food (String n, String t, double cal) {
		name = n;
		type = t;
		calorie = cal;
	}
	
	public Food (String n) {
		name = n;
		type = null;
		calorie = 0;
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public double getCalorie() {
		return calorie;
	}

	public boolean isSuitable (ArrayList<Food> diet, ArrayList<Double> limit) {
		
		ArrayList<Double> sum = new ArrayList<Double>();
		
		for (int i = 0; i < limit.size(); i++) {
			sum.add(0.0);
		}
		
		
		for (Food f: diet) {
			sum.set(0, sum.get(0) + f.calorie);			
		}
		
		int count = 0;
		for (Double nutrition: sum) {
			if (nutrition > limit.get(count)) {
				return false;
			}
			count++;
		}
		
		return true;
	}

}
