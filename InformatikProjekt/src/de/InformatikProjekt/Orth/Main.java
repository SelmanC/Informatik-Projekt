package de.InformatikProjekt.Orth;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.AMS.jBEAM.AbstractNumericChannel;
import com.AMS.jBEAM.DoubleChannel;
import com.AMS.jBEAM.InlineUserClassParentIF;
import com.AMS.jBEAM.InlineUserClassWorkerIF;
import com.AMS.jBEAM.jbDataObjectIF;

public class Main implements InlineUserClassWorkerIF {

	private InlineUserClassParentIF parent = null;

	private static int[] classes;
	private Map calculatedValue = null;
	private List<String> lines = null;
	private static double maxValue = 0;
	private static double minValue = 0;
	private static final int CLASS_RANGE = 5;

	@Override
	public void init(InlineUserClassParentIF parent) {
		this.parent = parent;
	}

	@Override
	public void validate() {
		calculatedValue = new HashMap<Integer, Integer>();
		lines = new ArrayList<>();
		jbDataObjectIF[] inputObjects = parent.getInputObjects();
		jbDataObjectIF[] resultObjects = parent.getResultObjects();

		DoubleChannel resultChannel = (DoubleChannel) resultObjects[0];
		AbstractNumericChannel inputChannel = (DoubleChannel) inputObjects[0];
		resultChannel.clearError();

		if (resultChannel == null || inputChannel == null) {
			return;
		}
		
		double[] values = getValues(inputChannel);	

		classes = getClasses();
		
		Map<Integer, Integer> calculatedMap = (Map<Integer, Integer>) calculate(values);

		resultChannel.adaptMaxValues(10);

		lines.add("Calculated");
		Path file = Paths.get("C:/Users/nabiz_000/Desktop/log1.txt");
		try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lines.clear();
		for (int key : calculatedMap.keySet()) {
			double value = (double) calculatedMap.get(key);
			lines.add(key + ":" + (double) value);
			resultChannel.setValue(key, value);
		}
		
		lines.add("KeySetCOunt:" + calculatedMap.size());
		Path file2 = Paths.get("C:/Users/nabiz_000/Desktop/log2.txt");
		try {
			Files.write(file2, lines, Charset.forName("UTF-8"));
		} catch (IOException f) {
			// TODO Auto-generated catch block
			f.printStackTrace();
		}

		resultChannel.setUsedSize(10, true);
		calculatedValue = null;
		lines = null;
	}

	public Map<Integer, Integer> test(double[] values) {
		return (Map<Integer, Integer>) calculate(values);
	}

	@Override
	public void dispose() {
		parent = null;
	}
	
	private double[] getValues(AbstractNumericChannel inputChannel){
		double[] values = new double[inputChannel.getUsedSize()];
		
		for (int i = 0; i < inputChannel.getUsedSize(); i++) {
			String inputValue = inputChannel.getValueAsString(i).replace(".", "").replace(',', '.');
			if(inputValue.isEmpty()){
				continue;
			}

			double currentValue = Double.valueOf(inputValue);
			if (i == 0) {
				maxValue = currentValue;
				minValue = currentValue;
			} else {
				isMaxOrMinValue(currentValue);
			}
			values[i]= currentValue;
		}
		return values;
	}

	private void isMaxOrMinValue(double inputValue) {
		if (inputValue > maxValue) {
			maxValue = inputValue;
		}
		if (inputValue < minValue) {
			minValue = inputValue;
		}
	}

	private int[] getClasses() {
		int maxClassCount = (int)((maxValue - minValue) / CLASS_RANGE);
		lines.add("macClassC:" + maxClassCount);
		int[] classes = new int[maxClassCount];
		classes[0] = (int) (minValue - 1);
		classes[maxClassCount - 1] =(int) maxValue;
		
		int currentValue = (int)minValue- 1;
		for(int i = 0; i < maxClassCount; i++){
			currentValue = currentValue + CLASS_RANGE;
			lines.add(currentValue+ ",");
			classes[i] = currentValue;
		}
		lines.add(";max:" + maxValue);
		return classes;
	}

	private Map calculate(double[] values) {
		String line = "";
		for (int i = 1; i < values.length; i++) {
			lines.add(line);
			line = "";

			double oldValue = values[i - 1], newValue = values[i];
			int[] oldBounds = getCurrentBounds(oldValue, newValue);

			line += "oldValue:" + oldValue + "newValue:" + newValue + ";";
			if (oldBounds == null)
				continue;

			line += "oldBounds:" + oldBounds[0] + "," + oldBounds[1] + "," + oldBounds[2] + ";";
			Map<Integer, Integer> newClassCrossed = getCrossedClassesAfterUpperBound(oldBounds[2], newValue);
			line += "ClassCrossed:";
			for (Integer crossedClass : newClassCrossed.keySet()) {
				int newValueForClass = getNewValueForClass(crossedClass);
				calculatedValue.put(crossedClass, newValueForClass);
				line += crossedClass.toString() + "-" + newValueForClass + ",";
			}
		}
		return calculatedValue;
	}

	private int[] getCurrentBounds(double oldValue, double newValue) {
		if (oldValue < classes[0] && newValue >= classes[0]) {
			return new int[] {classes[1], classes[0], 0 };
		}
		for (int i = 1; i < classes.length; i++) {
			double upperBound = classes[i], lowerBound = classes[i - 1];
			if (lowerBound <= oldValue && oldValue < upperBound) {
				return new int[] {classes[i], classes[i - 1], i };
			}
		}
		return null;
	}

	private Map<Integer, Integer> getCrossedClassesAfterUpperBound(int upperBoundCound, double newValue) {
		Map<Integer, Integer> crossedClasses = new HashMap<>();
		for (int i = upperBoundCound; i < classes.length; i++) {
			if (newValue >= classes[i]) {
				crossedClasses.put(classes[i], getNewValueForClass(classes[i]));
			} else {
				break;
			}
		}
		return crossedClasses;
	}

	private int getNewValueForClass(int currentClass) {
		return (int) calculatedValue.getOrDefault(currentClass, 0) + 1;
	}

	private List getListFromCalculatedValue() {
		List<List> calculatedList = new ArrayList<>();
		for (Object key : calculatedValue.keySet()) {
			List currentKeyList = getCurrentKeyList((int) key);
			calculatedList.add(currentKeyList);
		}
		return calculatedList;
	}

	private List getCurrentKeyList(int key) {
		List currentKeyList = new ArrayList<String>();
		for (int i = 0; i < (int) calculatedValue.get(key); i++) {
			currentKeyList.add("");
		}
		currentKeyList.add(String.valueOf(key));
		return currentKeyList;
	}
}
