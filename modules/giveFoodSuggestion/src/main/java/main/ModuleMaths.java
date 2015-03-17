package main;

import java.util.HashMap;

public class ModuleMaths {
	
	public static HashMap<String, String> computeBmi(double height, double weight) {
		
		double bmi = weight/(height*height);
		String status = bmi < 18.5 ? "underweight" : (bmi <= 24.9 ? "normal" : (bmi <= 29.9 ? "overweight" : "obese"));
				
		HashMap<String, String> bmiInfo = new HashMap<String, String>();
		bmiInfo.put("bmi", Double.toString(bmi));
		bmiInfo.put("status", status);
		return  bmiInfo;
		
	}

}
