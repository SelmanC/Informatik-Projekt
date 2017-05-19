package de.InformatikProjekt.Orth;
import com.AMS.jBEAM.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main implements InlineUserClassWorkerIF{

	private InlineUserClassParentIF parent = null;
	
	private static int[] classes = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	

	@Override
	public void init(InlineUserClassParentIF parent) {
		 parent.writeToConsole("CompileTest: In methode init()");
	     this.parent = parent;
		
	}
	
	@Override
	public void validate() {
		parent.writeToConsole("CompileTest: In methode validate()");
		jbDataObjectIF[] inputObjects = parent.getInputObjects();
        jbDataObjectIF[] resultObjects = parent.getResultObjects();
        
        DoubleChannel resultChannel = (DoubleChannel)resultObjects[0];
        AbstractNumericChannel inputChannel = (DoubleChannel)inputObjects[0];
        List<String> values = new ArrayList<>();
        resultChannel.clearError();
        
        if (resultObjects[0] instanceof DoubleChannel == false) {
            resultObjects[0].setError("Wrong type of result data item");
            return;
        }

        for (int i = 0; i < inputChannel.getUsedSize(); i++) {
           values.add(inputChannel.getValueAsString(i));
        }       
        
		Algorithm alg = new Algorithm();
		alg.setCalculatedClasses(classes);
		Map<Integer, Integer> calculatedMap = (Map<Integer, Integer>) alg.calculate(values.toArray());
		for (int key : calculatedMap.keySet()) {
			resultChannel.setValue(key, calculatedMap.get(key));
		}		
	}

	@Override
	public void dispose() {
		parent.writeToConsole("CompileTest: In methode dispose()");
        parent = null;
	}


}
