package de.InformatikProjekt.Orth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
	private static double maxValue;
	private static final String propertyFileName = "C:/Users/Selman/Desktop/KGUZProp.properties";
	private static int CLASS_Count;
	private static boolean isAufwaerts;

	@Override
	public void init(InlineUserClassParentIF parent) {
		this.parent = parent;
	}

	@Override
	public void validate() {
		maxValue = 0;
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

		loadPropertieFile();
		
		double[] values = getValues(inputChannel);

		classes = getClasses();
		Map<Integer, Integer> calculatedMap = (Map<Integer, Integer>) calculate(values);

		resultChannel.adaptMaxValues(classes[classes.length - 1]);

		for (int key : calculatedMap.keySet()) {
			double value = (double) calculatedMap.get(key);
			lines.add(key +":" +value + "");
			resultChannel.setValue(key, value);
		}

		Path p = Paths.get("C:/Users/Selman/Desktop/log2.txt");

		try {
			Files.write(p, lines, Charset.forName("UTF-8"));
		} catch (Exception e) {
			return;
		}

		resultChannel.setUsedSize(classes[classes.length - 1], true);
		calculatedValue = null;
		lines = null;
	}

	private void loadPropertieFile() {

		Properties prop = new Properties();

		if (!Files.exists(Paths.get(propertyFileName))) {
			prop.setProperty("class_count", "20");
			prop.setProperty("aufwaerts", "true");
			
			FileOutputStream propertyFile = null;
			try {
				propertyFile = new FileOutputStream(propertyFileName);
				prop.store(propertyFile, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try {
					propertyFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} 
		else {
			InputStream input = null;
			
			try {
				input = new FileInputStream(propertyFileName);
				prop.load(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		CLASS_Count = Integer.valueOf(prop.getProperty("class_count"));
		isAufwaerts = Boolean.valueOf(prop.getProperty("aufwaerts"));
		
		
	}

	private double[] getValues(AbstractNumericChannel inputChannel) {
		double[] values = new double[inputChannel.getUsedSize()];

		for (int i = 0; i < inputChannel.getUsedSize(); i++) {
			String inputValue = inputChannel.getValueAsString(i).replace(".", "").replace(',', '.');
			if (inputValue.isEmpty()) {
				continue;
			}

			double currentValue = Double.valueOf(inputValue);
			if (currentValue < 0) {
				continue;
			}
			isMaxValue(currentValue);
			values[i] = currentValue;
		}
		return values;
	}

	private void isMaxValue(double inputValue) {
		if (inputValue > maxValue) {
			maxValue = inputValue;
		}
	}

	private int[] getClasses() {
		double classRange = maxValue / CLASS_Count;
		int[] classes = new int[CLASS_Count];
		classes[0] = 0;
		classes[CLASS_Count - 1] = (int) maxValue + 1;

		int currentValue = 0;
		for (int i = 1; i < CLASS_Count - 2; i++) {
			currentValue = (int) (currentValue + classRange);
			classes[i] = currentValue;
		}
		return classes;
	}

	private Map calculate(double[] values) {
		for (int i = 1; i < values.length; i++) {

			double oldValue = values[i - 1], newValue = values[i];
			int[] oldBounds = getCurrentBounds(oldValue, newValue);
			if (oldBounds == null)
				continue;

			Map<Integer, Integer> newClassCrossed = new HashMap<>();
			if(isAufwaerts){
				newClassCrossed = getCrossedClassesAfterUpperBound(oldBounds[2], newValue);
			}
			else{
				newClassCrossed = getCrossedClassesBeforeLowerBound(oldBounds[2], newValue);
			}
			for (Integer crossedClass : newClassCrossed.keySet()) {
				int newValueForClass = getNewValueForClass(crossedClass);
				calculatedValue.put(crossedClass, newValueForClass);
			}
		}
		return calculatedValue;
	}

	private int[] getCurrentBounds(double oldValue, double newValue) {
		if (isNotInClassRange(oldValue, newValue)) {
			return new int[] { classes[1], classes[0], isAufwaerts ? 1 : 0};
		}
		for (int i = 1; i < classes.length; i++) {
			double upperBound = classes[i], lowerBound = classes[i - 1];
			if (isClassBoundsCrossed(oldValue, lowerBound, upperBound)) {
				return new int[] { classes[i], classes[i - 1], isAufwaerts ? i : i-1 };
			}
		}
		return null;
	}

	private boolean isNotInClassRange(double oldValue, double newValue) {
		if (isAufwaerts) {
			return oldValue < classes[0] && newValue >= classes[0];
		} else {
			return oldValue <= classes[0] && newValue > classes[0];
		}
	}

	private boolean isClassBoundsCrossed(double oldValue, double lowerBound, double upperBound) {
		if (isAufwaerts) {
			return lowerBound <= oldValue && oldValue < upperBound;
		} else {
			return lowerBound < oldValue && oldValue <= upperBound;
		}
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
	
	private Map<Integer, Integer> getCrossedClassesBeforeLowerBound(int lowerBoundCound, double newValue) {
		Map<Integer, Integer> crossedClasses = new HashMap<>();
		for (int i = lowerBoundCound; i > 0; i--) {
			if (newValue <= classes[i]) {
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
	
	public Map test(double[] values){
		return calculate(values);
	}

	@Override
	public void dispose() {
		parent = null;
	}
}
