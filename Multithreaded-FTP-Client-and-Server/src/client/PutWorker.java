package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PutWorker implements Runnable {

	private Socket socket;
	private int tPort;
	private int nPort;
	private String fileName;
	private String path;
	private String host;
	private DataOutputStream dOS;
	private DataInputStream dis;
	private static final String TERMINATE = "terminate";

	public PutWorker(String host, int nPort, int tPort, String fileName,
			String path) throws Exception {
		super();
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

		String currentDir = System.getProperty("user.dir");
		File file = new File(currentDir + "/" + fileName);
		if (!file.exists()) {
			System.out.println(fileName + ": No such file");
			file.delete();
			try {
				dOS.writeUTF("quit");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (file.isDirectory()) {
			System.out.println("Invalid operation: " + fileName
					+ " is a directory ");
			file.delete();
			try {
				dOS.writeUTF("quit");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				String notForUse = dis.readUTF();

				dOS.writeUTF("put");
				dOS.writeUTF(path);
				dOS.writeUTF(fileName);

				int commandID = dis.readInt();
				System.out.println("Command Id to terminate: " + commandID);

				FileInputStream fileStream = new FileInputStream(file);
				long fileSize = file.length();
				dOS.writeLong(fileSize);

				byte[] fileChunks = new byte[1000];
				int count = 0;
				while ((count = fileStream.read(fileChunks)) > 0) {
					if (ClientNPortWorker.clientCommandMap
							.containsKey(commandID)) {
						if (ClientNPortWorker.clientCommandMap.get(commandID)
								.equalsIgnoreCase(TERMINATE)) {
							ClientNPortWorker.clientCommandMap
							.remove(commandID);
							break;
						}
					}
					dOS.write(fileChunks, 0, count);
				}
				fileStream.close();
				dOS.writeUTF("quit");

			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}
}