/**
 * 
 */
package de.haw.hamburg.its.praktikum2.a2;

import java.nio.file.NoSuchFileException;

/**
 * @author Kevin Hüsgen
 *
 */
public class HC1STest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HC1S hc1s = new HC1S();
		try {
			hc1s.encrypt();
		} catch (NoSuchFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
