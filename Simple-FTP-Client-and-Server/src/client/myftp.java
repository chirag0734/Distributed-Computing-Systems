/**
 * This file contains the ftp client code
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * @author chirag
 * @author shubhi
 * 
 */
public class myftp {

	private static final String GET = "get";
	private static final String PUT = "put";
	private static final String DELETE = "delete";
	private static final String LS = "ls";
	private static final String CD = "cd";
	private static final String MKDIR = "mkdir";
	private static final String PWD = "pwd";
	private static final String QUIT = "quit";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Socket clientSocket;
		String host = null;
		int port = 0;

		try {
			if (args.length != 2) {
				System.out.println("Please provide a hostname and the port of the server to connect with");
				System.exit(1);
			} else {
				host = args[0];
				port = Integer.parseInt(args[1]);
			}
			//resolving the host name to IP address
			InetAddress address = InetAddress.getByName(host);

			clientSocket = new Socket(address, port);
			DataOutputStream dOS = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());

			Scanner sc = new Scanner(System.in);
			String input;
			//prompt screen will appear until "quit" command is fired.
			do {
				System.out.print("myftp>");
				
				input = sc.nextLine();

				String[] commands = input.split(" ");
				if (commands.length > 2) {
					System.out.println("Number of argument exceeded: Two arguments allowed");
					continue;
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
						//get command: to get a file from remote directory to local directory. If file already exists, it will update the file.		
						case GET: {
							dOS.writeUTF(command);
							dOS.writeUTF(inputFile);
							String currentDir = System.getProperty("user.dir");
							FileOutputStream fileWrite = null;
							String read;
							int endFile = 0;
							String response = (String) dIS.readUTF();
							if (response.equals("error")) {
								System.out.println(inputFile + ": No such file");
								break;
							} else if (response.equals("directory")) {
								System.out.println("Invalid operation: " + inputFile + " is a directory ");
								break;
							} else if (response.equals("ready")) {
								File file = new File(currentDir + "/" + inputFile);
								fileWrite = new FileOutputStream(file);
								do {
									read = (String) dIS.readUTF();
									endFile = Integer.parseInt(read);
									if (endFile != -1) {
										fileWrite.write(endFile);
									}
								} while (endFile != -1);
								fileWrite.close();
								break;
							}
						}
						//put command: to put a file from local directory to remote directory. If file already exists, it will update the file. 
						case PUT: {
							String currentDir = System.getProperty("user.dir");
							File fileName = new File(currentDir + "/" + inputFile);
							if (!fileName.exists()) {
								System.out.println(inputFile + ": No such file");
								fileName.delete();
								break;
							} else if (fileName.isDirectory()) {
								System.out.println("Invalid operation: " + inputFile + " is a directory ");
								fileName.delete();
								break;
							} else {
								dOS.writeUTF(command);
								dOS.writeUTF(inputFile);
								FileInputStream file = new FileInputStream(fileName);
								int fileEnd;
								do {
									fileEnd = file.read();
									dOS.writeUTF(String.valueOf(fileEnd));
								} while (fileEnd != -1);
								file.close();
								break;
							}
						}
						case CD: {
							dOS.writeUTF(command);
							dOS.writeUTF(inputFile);
							String severResponse = dIS.readUTF();
							if (severResponse.equals("error")) {
								System.out.println(CD + ": " + inputFile + ": Not a directory");
								break;
							} else {
								break;
							}
						}
						case DELETE: {
							dOS.writeUTF(command);
							dOS.writeUTF(inputFile);
							String severResponse = dIS.readUTF();
							if (severResponse.equals("error")) {
								System.out.println(DELETE + ": " + inputFile + ": No such file or directory");
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
								System.out.println(MKDIR + ": " + "cannot create directory '" + inputFile + "': File exists");
								break;
							} else {
								break;
							}
						}
						default:
							System.out.println("Not a valid command");
					}
				}

			} while (!input.equalsIgnoreCase(QUIT));
			clientSocket.close();
		} catch (IOException f) {
			System.out.println("IOException: " + f);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

}
