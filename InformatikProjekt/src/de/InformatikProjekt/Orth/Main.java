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

	private static int[] classes = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private Map calculatedValue = new HashMap<Integer, Integer>();
	private List<String> lines = new ArrayList<>();

	@Override
	public void init(InlineUserClassParentIF parent) {
		this.parent = parent;

	}

	@Override
	public void validate() {
		jbDataObjectIF[] inputObjects = parent.getInputObjects();
		jbDataObjectIF[] resultObjects = parent.getResultObjects();

		DoubleChannel resultChannel = (DoubleChannel) resultObjects[0];
		AbstractNumericChannel inputChannel = (DoubleChannel) inputObjects[0];
		List<String> values = new ArrayList<>();
		resultChannel.clearError();

		if (resultObjects[0] instanceof DoubleChannel == false || resultChannel == null || inputChannel == null) {
			return;
		}

		for (int i = 0; i < inputChannel.getUsedSize(); i++) {
			values.add(inputChannel.getValueAsString(i));
		}

		Map<Integer, Integer> calculatedMap = (Map<Integer, Integer>) calculate(values.toArray());


		lines.add("Calculated");
		Path file = Paths.get("C:/Users/Selman/Desktop/log1.txt");
		try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lines.clear();
		for (int key : calculatedMap.keySet()) {
			lines.add(key + ":" + calculatedMap.get(key));
			//resultChannel.setValue(key, calculatedMap.get(key));
		}
		Path file2 = Paths.get("C:/Users/Selman/Desktop/log2.txt");
		try {
			Files.write(file2, lines, Charset.forName("UTF-8"));
		} catch (IOException f) {
			// TODO Auto-generated catch block
			f.printStackTrace();
		}
		
	}

	public Map<Integer, Integer> test(Object[] values) {
		return (Map<Integer, Integer>) calculate(values);
	}

	@Override
	public void dispose() {
		parent = null;
	}

	private Map calculate(Object[] values) {
		for (int i = 1; i < values.length; i++) {
			if (values[i - 1].toString().isEmpty() || values[i].toString().isEmpty()) {
				continue;
			}
			String oldString = values[i - 1].toString()
					.replace(".", "")
					.replace(',', '.');
			String newString = values[i].toString()
					.replace(".", "")
					.replace(',', '.');

			lines.add(values[i].toString() + ":" + newString);

			if (oldString.isEmpty() || newString.isEmpty()) {
				continue;
			}

			double oldValue = values[i - 1] instanceof String ? Double.valueOf(oldString) : (double) values[i - 1],
					newValue = values[i] instanceof String ? Double.valueOf(newString) : (double) values[i];
			int[] oldBounds = getCurrentBounds(oldValue, newValue);
			if (oldBounds == null)
				continue;
			Map<Integer, Integer> newClassCrossed = getCrossedClassesAfterUpperBound(oldBounds[2], newValue);
			for (Object crossedClass : newClassCrossed.keySet()) {
				calculatedValue.put((int) crossedClass, getNewValueForClass((int) crossedClass));
			}
		}
		// calculatedList = getListFromCalculatedValue();
		return calculatedValue;
	}

	private int[] getCurrentBounds(double oldValue, double newValue) {
		for (int i = 1; i < classes.length; i++) {
			int upperBound = classes[i], lowerBound = classes[i - 1];
			if (lowerBound <= oldValue && oldValue < upperBound) {
				return new int[] { (int) classes[i], (int) classes[i - 1], i };
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
