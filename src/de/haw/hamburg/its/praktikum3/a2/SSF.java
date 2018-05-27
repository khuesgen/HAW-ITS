/**
 * 
 */
package de.haw.hamburg.its.praktikum3.a2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * @author Kevin Hüsgen
 *
 */
public class SSF {
	
	private static byte[] encryptedAlgorithmParameters;

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("Usage: java SSF privaterSchlüssel.prv öffentlicherSchlüssel.pub Datei verschlüsselteDatei.ssf");
			return;
		}
		
		System.out.println("Privater Schlüssel: " + args[0]);
		System.out.println("Öffentlicher Schlüssel: " + args[1]);
		System.out.println("Zu verschlüsselnde Datei: " + args[2]);
		System.out.println("Name der zu erzeugenden verschlüsselten Datei: " + args[3]);
		
		byte[] privateKeyBytes = readKeyFile(args[0]);
		byte[] publicKeyBytes = readKeyFile(args[1]);
		
		try {
			PrivateKey privateKey = generatePrivateKey(privateKeyBytes);
			PublicKey publicKey = generatePublicKey(publicKeyBytes);
			SecretKey AESKey = generateAESKey();
			byte[] signatureSecretKey = signSecretKey(privateKey, AESKey);
			byte[] encryptedSecretKey = encryptSecretKey(privateKey, publicKey, AESKey);
			byte[] encryptedInputFile = encryptFile(args[2], AESKey);
			writeFile(args[3],encryptedSecretKey, signatureSecretKey, encryptedAlgorithmParameters ,encryptedInputFile);
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
	
	public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(256);
		return keyGenerator.generateKey();
	}
	
	public static byte[] signSecretKey(PrivateKey privateKey, SecretKey AESKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature = Signature.getInstance("SHA512withRSA");
		signature.initSign(privateKey);
		signature.update(AESKey.getEncoded());
		return signature.sign();
	}
	
	public static byte[] encryptSecretKey(PrivateKey privateKey, PublicKey publicKey, SecretKey AESKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] firstBlock = cipher.update(AESKey.getEncoded());
		byte[] finalBlock = cipher.doFinal();
		byte[] encryptedSecretKey = new byte[firstBlock.length + finalBlock.length];
		System.arraycopy(firstBlock, 0, encryptedSecretKey, 0, firstBlock.length);
		System.arraycopy(finalBlock, 0, encryptedSecretKey, firstBlock.length, finalBlock.length);
		return encryptedSecretKey;
	}
	
	public static byte[] encryptFile(String fileName, SecretKey AESKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
		byte[] encryptedFileBytes = new byte[0];
		FileInputStream inputStream = new FileInputStream(fileName);
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, AESKey);
		byte[] buffer = new byte[16];
		
		int length; 
		while ((length = inputStream.read(buffer)) >0) {
			// Concat
			byte[] temp = new byte[encryptedFileBytes.length + length];
			System.arraycopy(encryptedFileBytes, 0, temp, 0, encryptedFileBytes.length);
			byte[] next = cipher.update(buffer, 0, length);
			System.arraycopy(next, 0, temp, encryptedFileBytes.length, next.length);
			encryptedFileBytes = temp;
		}
		
		cipher.doFinal();
		inputStream.close();
		encryptedAlgorithmParameters = cipher.getParameters().getEncoded();		
		return encryptedFileBytes;
	}
	
	public static void writeFile(String outputFileName, byte[] encryptedSecretKey, byte[] signatureSecretKey, byte[] encryptedAlgorithmParameters, byte[] encryptedInputFile) throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(outputFileName));
		dataOutputStream.writeInt(encryptedSecretKey.length);
		dataOutputStream.write(encryptedSecretKey);
		dataOutputStream.writeInt(signatureSecretKey.length);
		dataOutputStream.write(signatureSecretKey);
		dataOutputStream.writeInt(encryptedAlgorithmParameters.length);
		dataOutputStream.write(encryptedAlgorithmParameters);
		dataOutputStream.write(encryptedInputFile);
		dataOutputStream.close();
	}

}
