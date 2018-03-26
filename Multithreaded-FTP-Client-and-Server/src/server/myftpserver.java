/**
 * This file contains the ftp server code 
 */
package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

/**
 * @author chirag
 * @author shubhi
 *
 */
public class myftpserver {
	private Socket clientSocket;

	private static Path path;

	public myftpserver(Socket clientSocket) {
		super();
		this.clientSocket = clientSocket;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerSocket normalSocket = null, terminateSocket = null;
		int nPort = 0, tPort = 0;
		try {
			if (args[0] != null && args[1] != null) {
				nPort = Integer.parseInt(args[0]);
				tPort = Integer.parseInt(args[1]);
			} else
				System.out.println("Please provide port numbers");

			if ((nPort < 1024 && nPort > 65535) && (tPort < 1024 && tPort > 65535)) {
				System.out.println("Please provide valid port numbers");
				System.exit(1);
			} else if (nPort == tPort) {
				System.out.println("Please provide different port numbers");
			} else {
				normalSocket = new ServerSocket(nPort);
				terminateSocket = new ServerSocket(tPort);
				System.out.println("Server Initialized");
			}

			new Thread(new ServerNPortMediator(normalSocket)).start();
			new Thread(new ServerTPortMediator(terminateSocket)).start();

		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
