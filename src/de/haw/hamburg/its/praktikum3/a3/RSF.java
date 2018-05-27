/**
 * 
 */
package de.haw.hamburg.its.praktikum3.a3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Kevin Hüsgen
 *
 */
public class RSF {
	
	private static AlgorithmParameters algorithmParameters;
	private static byte[] encryptedSecretKey;
	private static byte[] decryptedSecretKey;
	private static byte[] signature;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("Usage: java RSF privaterSchlüssel.prv öffentlicherSchlüssel.pub verschlüsselteDatei.ssf entschlüsselteDatei");
			return;
		}
		
		System.out.println("Privater Schlüssel: " + args[0]);
		System.out.println("Öffentlicher Schlüssel: " + args[1]);
		System.out.println("Verschlüssselte Datei: " + args[2]);
		System.out.println("Name der zu erzeugenden entschlüsselten Datei: " + args[3]);
		
		byte[] privateKeyBytes = readKeyFile(args[0]);
		byte[] publicKeyBytes = readKeyFile(args[1]);
		
		try {
			PrivateKey privateKey = generatePrivateKey(privateKeyBytes);
			PublicKey publicKey = generatePublicKey(publicKeyBytes);
			DataInputStream dataInputStream = getData(args[2]);
			decryptedSecretKey = decryptSecretKey(encryptedSecretKey, privateKey);
			if (checkSignature(signature, publicKey, decryptedSecretKey)) {
				decryptFile(args[3], decryptedSecretKey, algorithmParameters, dataInputStream);
			}
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Eine benötigte Implementierung wurde nicht gefunden: " + e.getMessage());
		} catch (InvalidKeyException e) {
			System.err.println("Ein Key ist ungültig. Falscher Algorithmus? " + e.getMessage());
		} catch (SignatureException e) {
			System.err.println("Fehler beim ueberpruefen der Signatur!: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
			System.err.println("Benötigter Padding-Mechanismus wurde nicht gefunden: " + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.err.println("Eingabe ist kein vielfaches von 16: " + e.getMessage());
		} catch (BadPaddingException e) {
			System.err.println("Padding nicht valide. Falscher Schlüssel? " + e.getMessage());
		} catch (InvalidKeySpecException e) {
			System.err.println("Fehler beim Konvertieren des Schluessels: " + e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("Falsche Algorithmusparameter: " + e.getMessage());
		}
		
	}
	
	public static byte[] readKeyFile(String keyFileName) throws IOException {
		DataInputStream inputStream = new DataInputStream(new FileInputStream(keyFileName));
		int nameLenght = inputStream.readInt();
		inputStream.skipBytes(nameLenght);
		
		int keyLength = inputStream.readInt();
		byte[] key = new byte[keyLength];
		inputStream.read(key);
		inputStream.close();
		
		return key;
	}
	
	public static PrivateKey generatePrivateKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		PKCS8EncodedKeySpec pkcs8 =  new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(pkcs8);		
	}
	
	public static PublicKey generatePublicKey(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(x509);
	}
	
	public static DataInputStream getData(String encryptedFileName) throws IOException, NoSuchAlgorithmException {
		DataInputStream dataInputStream = new DataInputStream(new FileInputStream(encryptedFileName));
		
		// Lese encryptedSecretKey
		int secretKeyLength = dataInputStream.readInt();
		encryptedSecretKey = new byte[secretKeyLength];
		dataInputStream.readFully(encryptedSecretKey);
		
		// Lese signature
		int signatureLength = dataInputStream.readInt();
		signature = new byte[signatureLength];
		dataInputStream.readFully(signature);
		
		//Lese algorithmParameters
		int algorithmParameterLength = dataInputStream.readInt();
		byte[] algorithmParameter = new byte[algorithmParameterLength];
		dataInputStream.readFully(algorithmParameter);
		
		algorithmParameters = AlgorithmParameters.getInstance("AES");
		algorithmParameters.init(algorithmParameter);
		
		return dataInputStream;
		
	}
	
	
	public static byte[] decryptSecretKey(byte[] encryptedSecretKey, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(encryptedSecretKey);
	}
	
	public static boolean checkSignature(byte[] signature, PublicKey publicKey, byte[] decryptedSecretKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sha512Signature = Signature.getInstance("SHA512withRSA");
		sha512Signature.initVerify(publicKey);
		sha512Signature.update(decryptedSecretKey);
		return sha512Signature.verify(signature);
	}
	
	
	public static void decryptFile(String decryptedFileName, byte[] decryptedSecretKey, AlgorithmParameters algorithmParameter, DataInputStream dataInputStream) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, BadPaddingException {
		FileOutputStream dataOutputStream = new FileOutputStream(decryptedFileName);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		SecretKeySpec secretKeySpec = new SecretKeySpec(decryptedSecretKey, "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, algorithmParameter);
		
		byte[] buffer = new byte[16];
		int length; 
		while((length = dataInputStream.read(buffer)) > 0) {
			dataOutputStream.write(cipher.update(buffer, 0, length));
		}
		
		cipher.doFinal();
		dataInputStream.close();
		dataOutputStream.close();
	}
}
