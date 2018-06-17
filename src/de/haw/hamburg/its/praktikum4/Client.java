package de.haw.hamburg.its.praktikum4;

/* Simulation einer Kerberos-Session mit Zugriff auf einen Fileserver
 /* Client-Klasse
 */

import java.util.*;

public class Client extends Object {

	private KDC myKDC; // Konstruktor-Parameter

	private String currentUser; // Speicherung bei Login nötig
	private Ticket tgsTicket = null; // Speicherung bei Login nötig
	private long tgsSessionKey; // K(C,TGS) // Speicherung bei Login nötig

	// Konstruktor
	public Client(KDC kdc) {
		myKDC = kdc;
	}

	public boolean login(String userName, char[] password) {
		/* ToDo */
		String tgsServer = "myTGS";
		long nonce = generateNonce();
		TicketResponse tgsTicketResponse = myKDC.requestTGSTicket(userName, tgsServer, nonce);
		if (tgsTicketResponse == null) {
			System.out.println("Invalid login.");
			return false;
		}
		this.currentUser = userName;

		// "decrypt"
		long decryptKey = generateSimpleKeyFromPassword(password);
		if (!tgsTicketResponse.decrypt(decryptKey)) {
			tgsTicketResponse.printError("Decrypting with key " + decryptKey + " failed.");
			return false;
		}

		// check for replay attack
		if (nonce != tgsTicketResponse.getNonce()) {
			tgsTicketResponse.printError("Invalid nonce.");
			return false;
		}

		this.tgsTicket = tgsTicketResponse.getResponseTicket();
		this.tgsSessionKey = tgsTicketResponse.getSessionKey();
		tgsTicketResponse.print();
		return true;
	}

	public boolean showFile(Server fileServer, String filePath) {
		/* ToDo */
		long nonce = generateNonce();
		Auth auth = new Auth(currentUser, System.currentTimeMillis());
		auth.encrypt(tgsSessionKey);
		auth.print();
		TicketResponse serverTicketResponse = myKDC.requestServerTicket(tgsTicket, auth, fileServer.getName(), nonce);

		if (serverTicketResponse == null) {
			System.out.println("Invalid login.");
			return false;
		}

		if (!serverTicketResponse.decrypt(tgsSessionKey)) {
			serverTicketResponse.printError("Invalid session key.");
			return false;
		}

		if (nonce != serverTicketResponse.getNonce()) {
			serverTicketResponse.printError("Invalid nonce.");
			return false;
		}
		serverTicketResponse.print();

		// approach server and show file
		Auth serverAuth = new Auth(currentUser, System.currentTimeMillis());
		serverAuth.encrypt(serverTicketResponse.getSessionKey());
		serverAuth.print();
		return fileServer.requestService(serverTicketResponse.getResponseTicket(), serverAuth, "showFile", filePath);

	}

	/* *********** Hilfsmethoden **************************** */

	private long generateSimpleKeyFromPassword(char[] passwd) {
		// Liefert einen eindeutig aus dem Passwort abgeleiteten Schlüssel
		// zurück, hier simuliert als long-Wert
		long pwKey = 0;
		if (passwd != null) {
			for (int i = 0; i < passwd.length; i++) {
				pwKey = pwKey + passwd[i];
			}
		}
		return pwKey;
	}

	private long generateNonce() {
		// Liefert einen neuen Zufallswert
		long rand = (long) (100000000 * Math.random());
		return rand;
	}
}
