package de.InformatikProjekt.Orth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KGUEZ{
	
	private int[] classes;
	private Map calculatedValue;
	private List calculatedList;
	
	public KGUEZ() {
		this.calculatedValue = new HashMap<Integer, Integer>();
	}
	
	public void setCalculatedClasses(int[] classes) {
		this.classes = classes;
	}
	
	public Map calculate(Object[] values) {		
		for(int i = 1; i < values.length; i++){
			double oldValue = values[i-1] instanceof String? Double.valueOf((String)values[i-1]) : (double)values[i-1], 
				   newValue = values[i-1] instanceof String? Double.valueOf((String)values[i]) : (double)values[i];
		    Bounds oldBounds = getCurrentBounds(oldValue, newValue);
		    if(oldBounds == null) continue;
			Map<Integer, Integer> newClassCrossed = getCrossedClassesAfterUpperBound(oldBounds, newValue);
			for(Object crossedClass : newClassCrossed.keySet()){
				calculatedValue.put((int)crossedClass, getNewValueForClass((int)crossedClass));
			}
		}		
	//	calculatedList = getListFromCalculatedValue();
		return calculatedValue;	
	}
	
	private Bounds getCurrentBounds(double oldValue, double newValue){
		for(int i = 1; i < classes.length; i++){
			int upperBound = classes[i], lowerBound = classes[i-1];
			if(lowerBound <= oldValue && oldValue < upperBound){
				return new Bounds((int)classes[i], (int)classes[i-1], i);
			}
		}
		return null;
	}
	
	private Map<Integer, Integer> getCrossedClassesAfterUpperBound(Bounds bounds, double newValue){
		Map<Integer, Integer> crossedClasses = new HashMap<>();
		for(int i = bounds.upperBoundCount; i < classes.length; i++){
			if(newValue >= classes[i]){
				crossedClasses.put(classes[i], getNewValueForClass(classes[i]));
			}
			else{
				break;
			}
		}
		return crossedClasses;
	}
	
	private int getNewValueForClass(int currentClass){
		return (int)calculatedValue.getOrDefault(currentClass, 0) + 1;
	}
	
	public List getListFromCalculatedValue(){
		List<List> calculatedList = new ArrayList<>();
		for(Object key : calculatedValue.keySet()){
			List currentKeyList = getCurrentKeyList((int)key);
			calculatedList.add(currentKeyList);
		}
		return calculatedList;
	}
	
	private List getCurrentKeyList(int key){
		List currentKeyList = new ArrayList<String>();
		for(int i = 0; i < (int)calculatedValue.get(key); i++){
			currentKeyList.add("");
		}
		currentKeyList.add(String.valueOf(key));
		return currentKeyList;
	}
	
	private class Bounds{
		public int upperBound, lowerBound, upperBoundCount;
		
		public Bounds(int upperBound, int lowerBound, int upperBoundCount){
			this.upperBound = upperBound;
			this.lowerBound = lowerBound;			
			this.upperBoundCount = upperBoundCount; 
		}
	}

}
