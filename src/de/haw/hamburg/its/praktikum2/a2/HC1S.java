/**
 * 
 */
package de.haw.hamburg.its.praktikum2.a2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Scanner;

import de.haw.hamburg.its.praktikum2.a1.LCG;

/**
 * @author Kevin Hüsgen
 *
 */
public class HC1S {
	
public void encrypt() throws NoSuchFileException {
		
		Scanner s = new Scanner(System.in);
		System.out.println("Bitte den Pfad eingeben:");
		Path pfad = new File(s.next()).toPath();
		System.out.println("Bitte den Startwert eingeben:");
		int startwert = s.nextInt();
		s.close();
		SecureRandom sr = new SecureRandom();
		sr.setSeed(startwert);
		
		if (!Files.exists(pfad)) {
			throw new NoSuchFileException("Bitte eine existierende Datei angeben!");
		}
		
		try {
			byte[] plainData = Files.readAllBytes(pfad);
			byte[] encryptedData = new byte[plainData.length];		
			
			for (int i = 0; i < plainData.length; i++) {
				int key = sr.nextInt();
				encryptedData[i] = (byte) (plainData[i] ^ key);
			}
			
			FileOutputStream fileOutputStream = new FileOutputStream("crypt");
			fileOutputStream.write(encryptedData);
			
			System.out.println("Inhalt geschrieben nach: crypt");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
	}
	

}
