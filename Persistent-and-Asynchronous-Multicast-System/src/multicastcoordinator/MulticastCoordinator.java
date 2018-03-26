/**
 * This file contains the Multicast Coordinator code 
 */
package multicastcoordinator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.Synthesizer;

/**
 * @author chirag
 * @author shubhi
 *
 */
public class MulticastCoordinator implements Runnable {
	private Socket clientSocket;
	private Socket participantSocket;
	private static final String REGISTER = "register";
	private static final String DEREGISTER = "deregister";
	private static final String DISCONNECT = "disconnect";
	private static final String RECONNECT = "reconnect";
	private static final String MULTICAST_SEND = "msend";
	private static final String QUIT = "quit";
	private ParticipantDetails pDetails = null;
	private static int thresholdTime = 0;

	private static HashMap<Integer, ParticipantDetails> detailStorage = new HashMap<Integer, ParticipantDetails>();
	private HashMap<Timestamp, String> storedMessages = null;

	public MulticastCoordinator(Socket clientSocket) {
		super();
		this.clientSocket = clientSocket;
	}

	public MulticastCoordinator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerSocket coordinatorSocket = null;
		int coordinatorPort = 0;

		String fileName = null;
		try {
			if (args.length == 1 && args[0] != null) {
				fileName = args[0];

			} else
				System.out.println("Please provide an argument as filename");

			String currentDir = System.getProperty("user.dir");
			File file = new File(currentDir + "/multicastcoordinator/" + fileName);

			if (!file.exists()) {
				System.out.println("File doesn't exists");
			} else {

				BufferedReader b = new BufferedReader(new FileReader(file));

				String readLine = "";
				coordinatorPort = Integer.parseInt(b.readLine());
				thresholdTime = Integer.parseInt(b.readLine());
				coordinatorSocket = new ServerSocket(coordinatorPort);
				while (true) {
					Thread th = new Thread(new MulticastCoordinator(coordinatorSocket.accept()));
					th.start();
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			DataOutputStream dOS = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());
			String str;

			do {
				str = dIS.readUTF();
				switch (str.toLowerCase()) {

				case REGISTER: {
					int participantID = dIS.readInt();
					String participantIP = dIS.readUTF();
					String participantPort = dIS.readUTF();
					
					if (!detailStorage.isEmpty()) {
						if (detailStorage.containsKey(participantID)) {
							pDetails = new ParticipantDetails();
							pDetails = detailStorage.get(participantID);
							Boolean status = pDetails.getConnectionStatus();
							if (status) {
								dOS.writeUTF("Connected");
							} else {
								dOS.writeUTF("Disconnected");
							}

						} else {
							String result= checkForPort(participantPort,participantID);
							if(result.equalsIgnoreCase("Valid")){
								pDetails = new ParticipantDetails();
								pDetails = addParticipant(participantIP, participantPort);
								detailStorage.put(participantID, pDetails);
								dOS.writeUTF("Done");
								System.out.println("Registered:"+ participantID);
							}
							else{
								dOS.writeUTF("PortRegistered");
							}
							
						}
					} else {
						pDetails = new ParticipantDetails();
						pDetails = addParticipant(participantIP, participantPort);
						detailStorage.put(participantID, pDetails);
						dOS.writeUTF("Done");
						System.out.println("Registered:"+ participantID);
					}
					break;
				}

				case DEREGISTER: {

					int participantID = dIS.readInt();
					if (!detailStorage.isEmpty()) {
						if (detailStorage.containsKey(participantID)) {
							detailStorage.remove(participantID);
							dOS.writeUTF("Removed");
							System.out.println("Deregistered:"+participantID);
						} else {
							dOS.writeUTF("Error");
						}
					} else {
						dOS.writeUTF("Error");
					}
					
					break;
				}

				case DISCONNECT: {
					int participantID = dIS.readInt();
					if (!detailStorage.isEmpty()) {
						if (detailStorage.containsKey(participantID)) {
							pDetails = new ParticipantDetails();
							pDetails = detailStorage.get(participantID);
							pDetails.setConnectionStatus(false);
							detailStorage.replace(participantID, pDetails);
							dOS.writeUTF("Disconnected");
							System.out.println("Disconnected:"+participantID);
						} else {
							dOS.writeUTF("Error");
						}
					} else {
						dOS.writeUTF("Error");
					}
					
					break;
				}

				case RECONNECT: {
					int participantID = dIS.readInt();
					String participantPort = dIS.readUTF();
					if (!detailStorage.isEmpty()) {
						if (detailStorage.containsKey(participantID)) {
							pDetails = new ParticipantDetails();
							pDetails= detailStorage.get(participantID);
							
							String result= checkForPort(participantPort,participantID);
							if(result.equalsIgnoreCase("Valid")){
								dOS.writeUTF("Re-connected");
								System.out.println("Re-connected:"+participantID);
								pDetails.setConnectionStatus(true);
								pDetails.setParticipantPort(participantPort);
							}
							else{
								dOS.writeUTF("PortRegistered");
								break;
							}
							Thread.currentThread().sleep(1000);
							storedMessages = new HashMap<Timestamp, String>();
							storedMessages= pDetails.getStoredMessages();
							detailStorage.replace(participantID, pDetails);
							
							HashMap<Timestamp, String> newMessagesOne= new HashMap<Timestamp, String>();
							newMessagesOne.putAll(storedMessages);
							
							if (storedMessages!=null && !storedMessages.isEmpty()) {

								Timestamp timestamp = new Timestamp(System.currentTimeMillis());
								Timestamp timeWithThreshold = new Timestamp(
										timestamp.getTime() - (thresholdTime * 1000L));
								timeWithThreshold.setNanos(timestamp.getNanos());
								
								for (Timestamp t : newMessagesOne.keySet()) {
									if (t.after(timeWithThreshold) || t.equals(timeWithThreshold)) {
										try {
											participantSocket = new Socket(pDetails.getParticipantIP(),
													Integer.parseInt(participantPort));
											DataOutputStream outStream = new DataOutputStream(
													participantSocket.getOutputStream());
											outStream.writeUTF(newMessagesOne.get(t));
											storedMessages.remove(t);
											outStream.close();
											participantSocket.close();
										} catch (Exception e) {
											System.out.println(e);
											e.printStackTrace();
										}
									}
								}
								HashMap<Timestamp, String> newMessages= new HashMap<Timestamp, String>();
								pDetails.setStoredMessages(newMessages);
								break;
							}
						} else {
							dOS.writeUTF("Error");
						}
					} else {
						dOS.writeUTF("Error");
					}
					break;
				}

				case MULTICAST_SEND: {

					String message = dIS.readUTF();
					int participantID = dIS.readInt();
					if (!message.equals(null)) {
						if(detailStorage.containsKey(participantID)){
							if(detailStorage.get(participantID).getConnectionStatus()){
								dOS.writeUTF("messageReceived");
								sendMulticastMessage(message);
							}
							else{
								dOS.writeUTF("disconnected");
							}
						}
						else{
							dOS.writeUTF("Error");
						}
						
					}
					break;
				}

				case QUIT: {
					clientSocket.close();
					break;
				}

				}
			} while (!str.equalsIgnoreCase(QUIT));
			// clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String checkForPort(String participantPort,int participantID) {
		// TODO Auto-generated method stub
		
		for(int key: detailStorage.keySet()){
			if(key == participantID){
				if(participantPort.equals(detailStorage.get(key).getParticipantPort()) && detailStorage.get(key).getConnectionStatus())
					return "Invalid";
				else
					return "Valid";
			}else{
				if(participantPort.equals(detailStorage.get(key).getParticipantPort()))
					return "Invalid";
			else
				return "Valid";
			}
		}
		return "Valid";
	}

	public ParticipantDetails addParticipant(String participantIP, String participantPort) {

		pDetails = new ParticipantDetails();
		pDetails.setParticipantIP(participantIP);
		pDetails.setParticipantPort(participantPort);
		pDetails.setConnectionStatus(true);
		return pDetails;

	}

	public void sendMulticastMessage(String message) {

		if (!detailStorage.keySet().isEmpty()) {
			for (int key : detailStorage.keySet()) {

				pDetails = new ParticipantDetails();
				pDetails = detailStorage.get(key);
				if (pDetails.getConnectionStatus()) {
					try {
						participantSocket = new Socket(pDetails.getParticipantIP(),
								Integer.parseInt(pDetails.getParticipantPort()));
						DataOutputStream dOutput = new DataOutputStream(participantSocket.getOutputStream());
						dOutput.writeUTF(message);
						dOutput.close();
						participantSocket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					storedMessages = new HashMap<Timestamp, String>();
					storedMessages = pDetails.getStoredMessages();
					if(storedMessages==null){
						storedMessages = new HashMap<Timestamp, String>();
						storedMessages.put(timestamp, message);
						pDetails.setStoredMessages(storedMessages);
						detailStorage.replace(key, pDetails);
					}
					else{
						storedMessages.put(timestamp, message);
						pDetails.setStoredMessages(storedMessages);
						detailStorage.replace(key, pDetails);
					}
					
				}

			}
		}
	}

}
