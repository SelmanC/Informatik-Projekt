package de.InformatikProjekt.Orth.Test;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import de.InformatikProjekt.Orth.Main;

public class MainTest {

	@Test
	public void test() {
		Main main = new Main();
		String[] values = new String[]{"1", "2", "3", "4", "2", "9", "3", "2", "1", "5", "1", "9"};
		Map mapValue = main.test(values);

		for(Object s : mapValue.keySet()){
			if(s.equals(3)){
				assertEquals(mapValue.get(s), 4);
			}
		}
	}

}
