package participant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Scanner;

public class ParticipantAsClient implements Runnable {

	private static final String REGISTER = "register";
	private static final String DEREGISTER = "deregister";
	private static final String DISCONNECT = "disconnect";
	private static final String RECONNECT = "reconnect";
	private static final String MULTICAST_SEND = "msend";
	private static final String QUIT = "quit";
	public static Boolean flag = false;
	private ServerSocket serverSocket;

	private int participantId;
	private String messageFile;
	private String ip;
	private int coordinatorPort;
	private Socket participantSocket = null;

	public ParticipantAsClient(int participantId, String messageFileName,
			String ip, int coordinatorPort) throws Exception {
		// TODO Auto-generated constructor stub
		super();
		this.participantId = participantId;
		this.messageFile = messageFileName;
		this.ip = ip;
		this.coordinatorPort = coordinatorPort;
		participantSocket = new Socket(ip, coordinatorPort);
		// participantSocket.connect(ip, coordinatorPort);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// Thread.sleep(120);
			DataOutputStream dOS = new DataOutputStream(
					participantSocket.getOutputStream());
			DataInputStream dIS = new DataInputStream(
					participantSocket.getInputStream());

			Scanner sc = new Scanner(System.in);
			String input;
			// prompt screen will appear until "quit" command is fired.

			do {
				input = sc.nextLine();
				String[] commands = input.split(" ");
				if (commands.length == 0) {
					continue;
				} else if (commands.length < 2) {
					String command = commands[0];
					switch (command.toLowerCase()) {

					case DEREGISTER: {
						dOS.writeUTF(command);
						dOS.writeInt(participantId);
						String response = dIS.readUTF();
						if (response.equalsIgnoreCase("Error")) {
							System.out
									.println("Participant not yet registered.");
						}
						try {
							if (serverSocket != null)
								serverSocket.close();
							else
								continue;
						} catch (Exception e) {
							e.printStackTrace();
						}
						flag = true;
						break;
					}

					case DISCONNECT: {
						dOS.writeUTF(command);
						dOS.writeInt(participantId);
						String response = dIS.readUTF();
						if (response.equalsIgnoreCase("Error")) {
							System.out
									.println("Participant not yet registered.");
						} else {
							// kill thread-B
							try {
								serverSocket.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							flag = true;
						}
						break;

					}
					case QUIT: {
						dOS.writeUTF(command);
						flag = true;
						try {
							if (serverSocket != null)
								serverSocket.close();
						} catch (Exception e) {
							e.printStackTrace();
						}

						break;
					}
					default: {
						System.out.println("Not a valid command");
					}

					}
				} else {
					String command = commands[0];
					String inputCommand = commands[1];

					switch (command.toLowerCase()) {

					case REGISTER: {
						int port = Integer.parseInt(inputCommand);
						if (port < 1024 && port > 65535) {
							System.out
									.println("Please provide a valid port number");
							System.exit(1);
						}else if(port == coordinatorPort){
							System.out.println("Port already in use.");
							break;
						}

						dOS.writeUTF(command);
						dOS.writeInt(participantId);
						flag = false;
						String remoteIP = null;
						
						for (final Enumeration<NetworkInterface> interfaces = NetworkInterface
								.getNetworkInterfaces(); 
								interfaces.hasMoreElements();) {
							final NetworkInterface cur = interfaces.nextElement();

							if (cur.isLoopback()) {
								continue;
							}

							for (final InterfaceAddress address : cur.getInterfaceAddresses()) {
								final InetAddress inetaddr = address.getAddress();
								
								if ( !( inetaddr instanceof Inet4Address ) )
						        {
						            continue;
						        }

								remoteIP = inetaddr.getHostAddress();
							}
						}
						
						dOS.writeUTF(remoteIP);
						dOS.writeUTF(inputCommand);
						String response = dIS.readUTF();
						if (response.equalsIgnoreCase("Connected")) {
							System.out
									.println("Participant already registered and online.");
						} else if (response.equalsIgnoreCase("Disconnected")) {
							System.out
									.println("Participated registered but offline. Please Reconnect to receieve messages.");
						} else if (response.equalsIgnoreCase("PortRegistered")) {
							System.out
									.println("Port already registered.Please try another port. ");
						} else if (response.equalsIgnoreCase("Done")) {
							try {
								serverSocket = new ServerSocket(
										Integer.parseInt(inputCommand));

								new Thread(new ParticipantMediator(
										serverSocket, messageFile)).start();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					}

					case RECONNECT: {
						int port = Integer.parseInt(inputCommand);
						if (port < 1024 && port > 65535) {
							System.out
									.println("Please provide a valid port number");
							System.exit(1);
						}else if(port == coordinatorPort){
							System.out.println("Port already in use.");
							break;
						}

						dOS.writeUTF(command);
						dOS.writeInt(participantId);
						dOS.writeUTF(inputCommand);
						flag = false;
						String response = dIS.readUTF();
						if (response.equalsIgnoreCase("Re-connected")) {
							try {
								serverSocket = new ServerSocket(
										Integer.parseInt(inputCommand));
								new Thread(new ParticipantMediator(
										serverSocket, messageFile)).start();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (response.equalsIgnoreCase("Error")) {
							System.out
									.println("Participant not yet registered.");
						} else if (response.equalsIgnoreCase("PortRegistered")) {
							System.out
									.println("Port already registered.Please try another port. ");
						}
						break;

					}

					case MULTICAST_SEND: {
						dOS.writeUTF(command);
						dOS.writeUTF(inputCommand);
						dOS.writeInt(participantId);
						String acknowlegment = dIS.readUTF();
						if (acknowlegment.equalsIgnoreCase("Error")) {
							System.out
									.println("Participant not yet registered. Please register first and send message.");
						} else if (acknowlegment
								.equalsIgnoreCase("disconnected")) {
							System.out
									.println("Please reconnect to send message.");
						}

						break;
					}

					default:
						System.out.println("Not a valid command");
					}
				}

			} while (!input.equalsIgnoreCase(QUIT));
			dOS.close();
			dIS.close();
			participantSocket.close();
		} catch (IOException f) {
			System.out.println("IOException: " + f);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}

	}

}
