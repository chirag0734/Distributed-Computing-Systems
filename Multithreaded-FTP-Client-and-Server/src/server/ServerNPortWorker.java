package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;

public class ServerNPortWorker extends ServerPortWorker implements Runnable {
	private Socket clientSocket;
	private DataOutputStream dOS;
	private DataInputStream dis;
	private Path path;
	private HashMap<File,String> checkLock= null;
	private static Boolean flag=false;
	

	private static final String GET = "get";
	private static final String PUT = "put";
	private static final String DELETE = "delete";
	private static final String LS = "ls";
	private static final String CD = "cd";
	private static final String MKDIR = "mkdir";
	private static final String PWD = "pwd";
	private static final String QUIT = "quit";
	private static final String TERMINATE = "terminate";
	private static final String ACTIVE = "active";
	
	

	public ServerNPortWorker(Socket clientSocket) throws Exception {
		super();
		this.clientSocket = clientSocket;
		path = Paths.get(System.getProperty("user.dir"));

		dOS = new DataOutputStream(clientSocket.getOutputStream());
		dis = new DataInputStream(clientSocket.getInputStream());
		dOS.writeUTF(path.toString());
	}

	@Override
	public void run() {
		Thread t = Thread.currentThread();
		String name = t.getName();
		System.out.println(name+": Start");
		
		int commandID= generateCommandID();
		commandMap.put(commandID++, ACTIVE);
		
		try {
			// Input and Output stream of a socket

			String str;
			do {
				str = dis.readUTF();
				switch (str.toLowerCase()) {
				// get command: to get file from remote directory to local
				// directory
				case GET: {
					File fileName;
					String currentDir=dis.readUTF();
					String input = dis.readUTF();
					
					// When requested file doesn't exists : ERROR
					if (new File(currentDir + "/" + input).exists() == false) {
						dOS.writeUTF("error");
						break;
						// When requested file is a directory : ERROR
					} else if (new File(input).isDirectory()) {
						dOS.writeUTF("directory");
						break;
					} 
					 else {
							dOS.writeUTF("ready");
							int max= Collections.max(commandMap.keySet());
							int cmdID=max+1;
							//int cmdID= getLock(currentDir);
							dOS.writeInt(cmdID);
							
							while(!getLock(cmdID,currentDir)){
								Thread.sleep(5);
							}
							
	
							fileName = new File(currentDir + "/" + input);
							FileInputStream file = new FileInputStream(fileName);
							byte[] fileChunks= new byte[1000];
							int count=0;
							while ((count = file.read(fileChunks)) > 0)
							{
								if(commandMap.get(cmdID).equalsIgnoreCase(TERMINATE))
								{
									break;
								}
								else{
									dOS.write(fileChunks, 0, count);
								}
							}
							file.close();
							
							getUnLock(currentDir,cmdID);
							clientSocket.shutdownOutput();
							break;
						}
				}
				// put command: to put file from local directory to remote
				// directory
				case PUT: {

					String currentDir=dis.readUTF();
					String input = dis.readUTF();
					
					int max= Collections.max(commandMap.keySet());
					int cmdID=max+1;
					
					dOS.writeInt(cmdID);
					commandMap.put(cmdID,ACTIVE);
					writeLockQueue.add(cmdID);
					
					
					while(!putLock(cmdID,currentDir)){
						Thread.sleep(5);
					}
					
					
					FileOutputStream fileWrite = null;
					
					long fileSize= dis.readLong();
					File file = new File(currentDir + "/" + input);
					fileWrite = new FileOutputStream(file);

					byte[] fileChunks= new byte[1000];
					int chunkLen = 0;

					while (fileSize>0 && (chunkLen = dis.read(fileChunks,0,(int)Math.min(fileChunks.length, fileSize))) != -1) {
						if(commandMap.get(cmdID).equalsIgnoreCase(TERMINATE)){
							flag=true;
							if(file.exists()){
			    				file.delete();
			    			}
							break;
						}
						fileWrite.write(fileChunks,0,chunkLen);
						fileSize -=chunkLen;
					}
					fileWrite.close();
					putUnlock(cmdID,currentDir,flag);
					break;
				}
				case CD: {
					String folderName = dis.readUTF();
					if (folderName.equals("..")) {
						path = path.getParent();
						dOS.writeUTF("done");
						dOS.writeUTF(path.toString());
					} else {
						String newPath = path.toString().concat(
								"/" + folderName);
						if (new File(newPath).isDirectory()) {
							path = Paths.get(newPath);
							dOS.writeUTF("done");
							dOS.writeUTF(path.toString());
						} else {
							dOS.writeUTF("error");
						}
					}
					break;
				}
				// delete command: to delete a directory and all of its
				// sub-directories if any
				case DELETE: {
					String deleteFolder = dis.readUTF();
					String newPath = path.toString().concat("/" + deleteFolder);
					while(!deleteCheck(newPath)){
						Thread.sleep(5);
					}
					if (new File(newPath).exists()) {
						File file = new File(newPath);
						Boolean flag = file.delete();
						if (file.isDirectory() && !flag) {
							String[] list = file.list();
							for (String f : list) {
								if (new File(newPath + "/" + f).isDirectory()) {
									delete(new File(newPath + "/" + f), newPath
											+ "/" + f);
								} else {
									new File(newPath + "/" + f).delete();
								}
							}
							file.delete();
						}
						dOS.writeUTF("done");
						break;
					} else {
						dOS.writeUTF("error");
						break;
					}
					
				}
				// ls command: to list of files and directories in current
				// directory
				case LS: {
					File serverDir = new File(path.toString());
					String[] list = serverDir.list();
					String listOfFiles = String.join("   ", list);
					dOS.writeUTF(listOfFiles);
					break;
				}
				// mkdir command: to create a directory
				case MKDIR: {
					String makeFolder = dis.readUTF();
					String newFolder = path.toString().concat("/" + makeFolder);
					if (new File(newFolder).exists()) {
						dOS.writeUTF("folderExist");
					} else {
						File file = new File(newFolder);
						file.mkdir();
						dOS.writeUTF("done");
					}
					break;
				}
				// pwd command: to display path of current working directory
				case PWD: {
					dOS.writeUTF(path.toString());
					break;
				}
				case TERMINATE:{
					String cmdIDString = dis.readUTF();
					int cmdID = Integer.parseInt(cmdIDString);
					if(commandMap.containsKey(cmdID)){
						if(commandMap.get(cmdID).equals(TERMINATE)){
							dOS.writeUTF("error");
						}
						else{
						commandMap.replace(cmdID, TERMINATE);
						dOS.writeUTF("done");}
					}
					else{
						dOS.writeUTF("error");
					}
					break;
					
				}
				// quit command: to close the connection with server. Bye..
				// Bye..!!
				case QUIT: {
					System.out.println(Thread.currentThread().getName()+": Quit");
					clientSocket.close();
					break;
				}
				}
			} while (!str.equalsIgnoreCase(QUIT));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}public void delete(File file, String newPath) {
		String[] list = file.list();
		for (String f : list) {
			if (new File(newPath + "/" + f).isDirectory()) {
				delete(new File(newPath + "/" + f), newPath + "/" + f);
			} else {
				new File(newPath + "/" + f).delete();
			}
		}
		file.delete();
	}

}
