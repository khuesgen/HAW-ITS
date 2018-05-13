package de.haw.hamburg.its.praktikum2.a3;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Kevin Hüsgen
 *
 */
public class TripleDES {
	
	public static DES des1;
	public static DES des2;
	public static DES des3;

	
	public static void main(String[] args) throws Exception {
		//TODO handle < 4 args

		

		// Lese Dateien ein und erzeuge Byte Arrays
		byte[] sourceFile = Files.readAllBytes(Paths.get(args[0]));
		byte[] keyFile = Files.readAllBytes(Paths.get(args[1]));
		
		// Erstelle Ziel Byte Array und Datei
		byte[] target = new byte[sourceFile.length];
		File targetFile = new File(args[2]);

		
		// Kopiere Inhalt von der Quelle zum Ziel
		System.arraycopy(sourceFile, 0, target, 0, sourceFile.length);
		
		System.out.println("Source: " + Paths.get(args[0]));
		System.out.println("KeyFile: " + Paths.get(args[1]));
		

		// Erstelle die Keys (3 * 8 Byte) und den Initialisierungsvektor (1*8 Byte)
		byte[] key1 = Arrays.copyOfRange(keyFile, 0, 8); 
		byte[] key2 = Arrays.copyOfRange(keyFile, 8, 16);
		byte[] key3 = Arrays.copyOfRange(keyFile, 16, 24);	
		byte[] initialisierungsVektor = Arrays.copyOfRange(keyFile, 24, 32);
		
		System.out.println("DES Key1: " + Arrays.toString(key1));
		System.out.println("DES Key2: " + Arrays.toString(key2));
		System.out.println("DES Key3: " + Arrays.toString(key3));
		System.out.println("Initialization Vector: " + Arrays.toString(initialisierungsVektor));
		
		// Erstelle neue DES-Objekte mit den Keys aus dem keyfile				
		des1 = new DES(key1);
		des2 = new DES(key2);
		des3 = new DES(key3);
		
		
		if (args[3].equals("encrypt")) {
			System.out.println("Encrypting File...");
			encrypt(sourceFile, target, initialisierungsVektor);
		}
		else if (args[3].equals("decrypt")) {
			System.out.println("Decrypting File...");
			decrypt(sourceFile, target, initialisierungsVektor);			
		}
		else {
			System.out.println("Error - Please use either 'encrypt' or 'decrypt'");
		}
		
		// Schreibe die Datei
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
		bos.write(target);
		bos.flush();
		bos.close();
		System.out.println("File written to: " + targetFile.getAbsolutePath());
	}

	/**
	 * Entschlüsselt die angegebenen Bytes mithilfe des Initialisierungsvektors 
	 * 
	 * @param sourceFile
	 * @param target
	 * @param initialisierungsVektor
	 */
	public static void decrypt(byte[] sourceFile, byte[] target, byte[] initialisierungsVektor) {
		byte[] curr;
		// Plaintext buffer
		byte[] plain;
		// Chiffre buffer
		byte[] cipher;
		int offset;

		// aktuelle Bytes
		curr = encryptDecryptEncrypt(initialisierungsVektor);
		// klartext
		plain = new byte[8];
		//chiffre
		cipher = new byte[8];
		offset = 0;
		
		// Erhöhe nach jedem Durchlauf den Offset um 8 Bytes
		for (int x = 0; x < sourceFile.length; x += 8) {
			if (x + 8 > sourceFile.length - 1) {
				offset = sourceFile.length;
			}
			else {
				offset = x + 8;
			}
		
			cipher = Arrays.copyOfRange(target, x, offset);
			
			// CFB XOR
			plain = xor(curr, cipher);
			
			System.arraycopy(plain, 0, target, x, offset - x);
			curr = encryptDecryptEncrypt(cipher);
		}
	}

	/**
	 * @param sourceFile
	 * @param target
	 * @param initialisierungsVektor
	 */
	public static void encrypt(byte[] sourceFile, byte[] target, byte[] initialisierungsVektor) {
		byte[] curr;
		// Plaintext buffer
		byte[] plain;
		// Chiffre buffer
		byte[] cipher;
		int offset;
		
	
		curr = new byte[8];
		plain = new byte[8];
		cipher = encryptDecryptEncrypt(initialisierungsVektor);


		
		offset = 0;
		
		// Erhöhe nach jedem Durchlauf den Offset um 8 Bytes
		for (int x = 0; x < sourceFile.length; x += 8) {
			if (x + 8 > sourceFile.length - 1) {
				offset = sourceFile.length;
			}
			else {
				offset = x + 8;
			}
			
			// Befülle den PlaintextArray 
			plain = Arrays.copyOfRange(target, x, offset);
			
			// CFB XOR
			curr = xor(cipher, plain);
			System.arraycopy(curr, 0, target, x, offset - x);
			cipher = encryptDecryptEncrypt(curr);
		}
	}
	
	public static byte[] encryptDecryptEncrypt(byte[] source) {
		byte[] temp = new byte[8];
		System.arraycopy(source, 0, temp, 0, source.length);
		des1.encrypt(temp, 0, temp, 0);
		des2.decrypt(temp, 0, temp, 0);
		des3.encrypt(temp, 0, temp, 0);
		return temp;
	}
	
	
	public static byte[] xor(byte[] o1, byte[] o2) {
		byte[] temp = new byte[o1.length];
		
		for (int x = 0; x < o2.length; x++) {
			temp[x] = (byte)(o1[x] ^ o2[x]);
		}
		
		return temp;
	}
	
}