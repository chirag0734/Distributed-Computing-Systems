package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class ClientNPortWorker implements Runnable {
	private String host;
	private int nPort;
	private int tPort;
	private Socket clientSocket;
	private Path path;
	
	private static final String GET = "get";
	private static final String PUT = "put";
	private static final String DELETE = "delete";
	private static final String LS = "ls";
	private static final String CD = "cd";
	private static final String MKDIR = "mkdir";
	private static final String PWD = "pwd";
	private static final String QUIT = "quit";
	private static final String TERMINATE = "terminate";

	public static HashMap<Integer, String> clientCommandMap = new HashMap<Integer, String>();

	public ClientNPortWorker(String host, int nPort, int tPort)
			throws Exception {
		super();
		this.host = host;
		this.nPort = nPort;
		this.tPort = tPort;
		InetAddress address = InetAddress.getByName(host);

		clientSocket = new Socket();

		clientSocket.connect(new InetSocketAddress(address.getHostAddress(),
				nPort), 1000);
		path = Paths.get(System.getProperty("user.dir"));

	}

	@Override
	public void run() {

		Thread t = Thread.currentThread();
		String name = t.getName();
		// System.out.println("name= " + name);
		clientCommandMap.put(1, "ACTIVE");

		try {
			DataOutputStream dOS = new DataOutputStream(
					clientSocket.getOutputStream());
			DataInputStream dIS = new DataInputStream(
					clientSocket.getInputStream());
			String serverPath = dIS.readUTF();

			Scanner sc = new Scanner(System.in);
			String input;
			// prompt screen will appear until "quit" command is fired.

			do {
				Thread.sleep(200);
				System.out.print("myftp>");
				// Thread.sleep(10);
				input = sc.nextLine();

				String[] commands = input.split(" ");
				if (commands.length > 2) {
					String command = commands[0];
					String fileName = commands[1];
					String checkArgument = commands[2];
					if (command.equals("get")) {
						new Thread(new GetWorker(host, nPort, tPort, fileName,
								serverPath)).start();
						// Thread.sleep(50);
						// continue;
					} else if (command.equals("put")) {
						new Thread(new PutWorker(host, nPort, tPort, fileName,
								serverPath)).start();
						// Thread.sleep(50);
						// continue;
					} else {
						System.out
								.println("Number of argument exceeded: Two arguments allowed");
						continue;
					}

				} else if (commands.length == 0) {
					continue;
				} else if (commands.length < 2) {
					String command = commands[0];
					switch (command.toLowerCase()) {

					case LS: {
						dOS.writeUTF(command);
						String files = dIS.readUTF();
						System.out.println(files);
						break;
					}
					case PWD: {
						dOS.writeUTF(command);
						String path = dIS.readUTF();
						System.out.println(path);
						break;
					}
					case QUIT: {
						dOS.writeUTF(command);
						break;
					}
					default: {
						System.out.println("Not a valid command");
					}

					}
				} else {
					String command = commands[0];
					String inputFile = commands[1];
					switch (command.toLowerCase()) {
					case GET: {
						new Thread(new GetWorker(host, nPort, tPort, inputFile,
								serverPath)).start();
						Thread.sleep(50);
						break;
					}
					case PUT: {
						new Thread(new PutWorker(host, nPort, tPort, inputFile,
								serverPath)).start();
						Thread.sleep(50);
						break;
					}
					case CD: {
						dOS.writeUTF(command);
						dOS.writeUTF(inputFile);
						String severResponse = dIS.readUTF();
						if (severResponse.equals("error")) {
							System.out.println(CD + ": " + inputFile
									+ ": Not a directory");
							break;
						} else {
							serverPath = dIS.readUTF();
							break;
						}
					}
					case DELETE: {
						dOS.writeUTF(command);
						dOS.writeUTF(inputFile);
						dOS.writeUTF(path.toString());
						String severResponse = dIS.readUTF();
						if (severResponse.equals("error")) {
							System.out.println(DELETE + ": " + inputFile
									+ ": No such file or directory");
							break;
						} else {
							break;
						}
					}
					case MKDIR: {
						dOS.writeUTF(command);
						dOS.writeUTF(inputFile);
						String severResponse = dIS.readUTF();
						if (severResponse.equals("folderExist")) {
							System.out.println(MKDIR + ": "
									+ "cannot create directory '" + inputFile
									+ "': File exists");
							break;
						} else {
							break;
						}
					}
					case TERMINATE: {
						dOS.writeUTF(command);
						dOS.writeUTF(inputFile);

						clientCommandMap.put(Integer.parseInt(inputFile),
								TERMINATE);
						String status = dIS.readUTF();

						if (status.equalsIgnoreCase("error")) {
							System.out
									.println("Command ID already terminated or File already transferred.");
							break;
						} else {
							// System.out.println("Calling ClientTPortWorker thread");
							(new Thread(new ClientTPortWorker(host, tPort,
									inputFile))).start();
							break;
						}
					}

					default:
						System.out.println("Not a valid command");
					}
				}

			} while (!input.equalsIgnoreCase(QUIT));
			dOS.close();
			dIS.close();
			clientSocket.close();
		} catch (IOException f) {
			System.out.println("IOException: " + f);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}

	}
}
