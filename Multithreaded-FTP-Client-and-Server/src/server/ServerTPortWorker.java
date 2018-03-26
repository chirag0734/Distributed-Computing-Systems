package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerTPortWorker extends ServerPortWorker implements Runnable {
	private Socket clientSocket;
	private DataOutputStream dOS;
	private DataInputStream dIS;
	private Path path;

	private static final String TERMINATE = "terminate";
	private static final String ACTIVE = "active";

	public ServerTPortWorker(Socket clientSocket) throws Exception {
		super();
		this.clientSocket = clientSocket;

		path = Paths.get(System.getProperty("user.dir"));

		dOS = new DataOutputStream(clientSocket.getOutputStream());
		dIS = new DataInputStream(clientSocket.getInputStream());
	}

	@Override
	public void run() {
		Thread t = Thread.currentThread();
		String name = t.getName();
		try {
			String str = dIS.readUTF();

			switch (str.toLowerCase()) {
			case TERMINATE: {
				String cmdIDString = dIS.readUTF();
				int cmdID = Integer.parseInt(cmdIDString);
				commandMap.replace(cmdID, TERMINATE);
				break;
			}
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
