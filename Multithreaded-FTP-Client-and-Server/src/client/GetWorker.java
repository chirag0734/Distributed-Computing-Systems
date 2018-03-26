package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class GetWorker implements Runnable {

	private Socket socket;
	private int tPort;
	private int nPort;
	private String fileName;
	private String path;
	private String host;
	private DataOutputStream dOS;
	private DataInputStream dis;
	private static final String TERMINATE = "terminate";

	public GetWorker(String host, int nPort, int tPort, String fileName,
			String path) throws Exception {
		// TODO Auto-generated constructor stub
		super();
		// setDaemon(true);
		this.host = host;
		this.nPort = nPort;
		this.tPort = tPort;
		this.fileName = fileName;
		this.path = path;

		InetAddress address = InetAddress.getByName(host);

		socket = new Socket();
		socket.connect(new InetSocketAddress(address.getHostAddress(), nPort),
				1000);

		dOS = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());

	}

	@Override
	public void run() {

		try {
			String notForUse = dis.readUTF();

			String currentDir = System.getProperty("user.dir");
			FileOutputStream fileWrite = null;
			dOS.writeUTF("get");
			dOS.writeUTF(path);
			dOS.writeUTF(fileName);
			String response = (String) dis.readUTF();
			if (response.equals("error")) {
				System.out.println(fileName + ": No such file");
			} else if (response.equals("directory")) {
				System.out.println("Invalid operation: " + fileName
						+ " is a directory ");
			} else if (response.equals("ready")) {
				int commandID = dis.readInt();
				if (commandID == 0) {
					System.out.println(fileName + ": No such file exists");
				} else {
					// Thread.sleep(100);
					System.out.println("Command Id to terminate: " + commandID);
				}

				File file = new File(currentDir + "/" + fileName);
				fileWrite = new FileOutputStream(file);

				byte[] fileChunks = new byte[1000];
				int chunkLen = 0;

				while ((chunkLen = dis.read(fileChunks)) != -1) {
					if (ClientNPortWorker.clientCommandMap
							.containsKey(commandID)) {
						if (ClientNPortWorker.clientCommandMap.get(commandID)
								.equalsIgnoreCase(TERMINATE)) {
							ClientNPortWorker.clientCommandMap
							.remove(commandID);
							if (file.exists()) {
								file.delete();
							}
							break;
						}
					}
					fileWrite.write(fileChunks, 0, chunkLen);
				}
				fileWrite.close();

			}
			dOS.writeUTF("quit");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
