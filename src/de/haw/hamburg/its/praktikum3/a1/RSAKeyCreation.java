package de.haw.hamburg.its.praktikum3.a1;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAKeyCreation {
	
	private static KeyPair keyPair;
	private static String owner;
	private static byte[] publicKey;
	private static byte[] privateKey;


	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		if (args.length == 1) {
			owner = args[0];
			KeyPairGenerator kPG = KeyPairGenerator.getInstance("RSA");
			kPG.initialize(2048);
			keyPair = kPG.generateKeyPair();
			generatePublicKeyFile();
			generatePrivateKeyFile();
		}
		else {
			System.out.println("Usage: java RSAKeyCreation name");
		}			
	}
	
	private static void generatePublicKeyFile() throws IOException {
		publicKey = keyPair.getPublic().getEncoded();
		DataOutputStream publicKeyFile = new DataOutputStream(new FileOutputStream(owner + ".pub"));
		publicKeyFile.writeInt(owner.length());
		publicKeyFile.writeBytes(owner);
		publicKeyFile.writeInt(publicKey.length);
		publicKeyFile.write(publicKey);		
		publicKeyFile.close();
		System.out.println("Öffentlicher Schlüssel erstellt! Format: " + keyPair.getPublic().getFormat());
	}
	
	private static void generatePrivateKeyFile() throws IOException {
		privateKey = keyPair.getPrivate().getEncoded();
		DataOutputStream privateKeyFile = new DataOutputStream(new FileOutputStream(owner + ".prv"));
		privateKeyFile.writeInt(owner.length());
		privateKeyFile.writeBytes(owner);
		privateKeyFile.writeInt(privateKey.length);
		privateKeyFile.write(privateKey);
		privateKeyFile.close();
		System.out.println("Privater Schlüssel erstellt! Format: " + keyPair.getPrivate().getFormat());
	}
}