package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientTPortWorker implements Runnable {
	private int tPort;
	private Socket clientSocket;
	private String host;
	private Path path;
	private String commandID;

	private static final String TERMINATE = "terminate";

	public ClientTPortWorker(String host, int tPort, String commandID)
			throws Exception {
		super();
		this.tPort = tPort;
		this.host = host;
		this.commandID = commandID;

		InetAddress address = InetAddress.getByName(host);

		clientSocket = new Socket();

		clientSocket.connect(new InetSocketAddress(address.getHostAddress(),
				tPort), 1000);

		path = Paths.get(System.getProperty("user.dir"));
	}

	@Override
	public void run() {
		try {
			DataOutputStream dOS = new DataOutputStream(
					clientSocket.getOutputStream());
			DataInputStream dIS = new DataInputStream(
					clientSocket.getInputStream());

			dOS.writeUTF(TERMINATE);
			dOS.writeUTF(commandID);

			dOS.close();
			dIS.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
}
