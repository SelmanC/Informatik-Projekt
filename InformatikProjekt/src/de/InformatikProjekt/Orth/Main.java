package de.InformatikProjekt.Orth;

import java.util.List;

public class Main {

	private static String[] values = new String[] { "1", "4", "1", "2", "3", "4", "5.5", "5.6", "5.8", "4", "4", "4.4",
			"9.8", "9.2", "3.4", "1", "3", "4", "1", "3" };
	private static int[] classes = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	public static void main(String[] args) {

		Algorithm alg = new KGUEZ();
		alg.setCalculatedClasses(classes);
		List<List<String>> calculatedList = (List) alg.calculate(values);
		for (List<String> values : calculatedList) {
			String build = "";
			for (String value : values) {
				build += value + " ";
			}
			System.out.println(build);
		}
	}

}
