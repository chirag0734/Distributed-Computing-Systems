/**
 * This file contains the ftp client code
 */
package client;

import java.io.IOException;
import java.net.Socket;

/**
 * @author chirag
 * @author shubhi
 * 
 */
public class myftp {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Path path = Paths.get(System.getProperty("user.dir"));
		String host = null;
		int nPort = 0, tPort = 0;

		try {

			if (args.length != 3) {
				System.out
						.println("Please provide a hostname and the port numbers of the server to connect with ");
				System.exit(1);
			} else {
				host = args[0];
				nPort = Integer.parseInt(args[1]);
				tPort = Integer.parseInt(args[2]);
			}

			new Thread(new ClientNPortWorker(host, nPort, tPort)).start();

		} catch (IOException f) {
			System.out.println("IOException: " + f);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

}
