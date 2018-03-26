/**
 * This file contains the ftp server code 
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author chirag
 * @author shubhi
 *
 */
public class myftpserver implements Runnable{
	private Socket clientSocket;
	
	private static final String GET = "get";
	private static final String PUT = "put";
	private static final String DELETE = "delete";
	private static final String LS = "ls";
	private static final String CD = "cd";
	private static final String MKDIR = "mkdir";
	private static final String PWD = "pwd";
	private static final String QUIT = "quit";
	private static Path path;
	
	public myftpserver(Socket clientSocket) {
		super();
		this.clientSocket = clientSocket;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket;
		int port = 0;
		try{  
			 if(args[0] != null)
				 port = Integer.parseInt(args[0]);
			 else
				 System.out.println("Please provide a port number");
			 
			if( port < 1024 && port > 65535){
				System.out.println("Please provide a valid port number");
				System.exit(1);
			}else{
				serverSocket = new ServerSocket(port);
				System.out.println("Server Initialized");
				
				while(true){
					Thread th = new Thread(new myftpserver(serverSocket.accept()));
					th.start();
					path = Paths.get(System.getProperty("user.dir"));
					
				}
			}
		}catch(Exception e){System.out.println(e);}

	}

	public void run() {
		try{
			//Input and Output stream of a socket
			DataOutputStream dOS=new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream dis=new DataInputStream(clientSocket.getInputStream());
			String  str;
			
			do {
			 str=dis.readUTF(); 
			switch(str.toLowerCase()){
				//get command: to get file from remote directory to local directory
				case GET:{
					String input= dis.readUTF();
					String currentDir=path.toString();
					File fileName;
					//When requested file doesn't exists : ERROR
					if (new File(currentDir+"/"+input).exists()==false){
						dOS.writeUTF("error");
						break;
					//When requested file is a directory : ERROR
					} else if(new File(input).isDirectory()){
						dOS.writeUTF("directory");
						break;
					}else{
						dOS.writeUTF("ready");
						fileName = new File(currentDir+"/"+input);
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
				//put command: to put file from local directory to remote directory
				case PUT:{
					String input= dis.readUTF();
					String currentDir=path.toString();
					File file= new File(currentDir+"/"+input);
					FileOutputStream fileWrite=new FileOutputStream(file);
					String read;
					int endFile;
					do{
						read= (String)dis.readUTF();
						endFile= Integer.parseInt(read);
						if(endFile!=-1){
							fileWrite.write(endFile);
						}
					}while(endFile!=-1);
					fileWrite.close();
					break;
				}
				//cd command: to traverse through directories
				case CD:{
					String folderName= dis.readUTF();
					if(folderName.equals("..")){
						path=path.getParent();
						dOS.writeUTF("done");
					}
					else{
						String newPath= path.toString().concat("/"+folderName);
						if(new File(newPath).isDirectory()){
							
							path= Paths.get(newPath);
							dOS.writeUTF("done");
						}else{
							dOS.writeUTF("error");
						}
					}	
					break;
				}
				//delete command: to delete a directory and all of its sub-directories if any
				case DELETE:{
					String deleteFolder= dis.readUTF();
					String newPath= path.toString().concat("/"+deleteFolder);
					if(new File(newPath).exists()){
						File file= new File(newPath);
						Boolean flag = file.delete();
						if(file.isDirectory() && !flag){
							String[] list = file.list();
							for(String f : list){
								if(new File(newPath+"/"+f).isDirectory()){
									delete(new File(newPath+"/"+f), newPath+"/"+f);
								}else{
									new File(newPath+"/"+f).delete();
								}
							}
							file.delete();						
						}	
						dOS.writeUTF("done");
					}
					else{
						dOS.writeUTF("error");
					}
					break;
				}
				//ls command: to list of files and directories in current directory
				case LS:{
					File serverDir= new File(path.toString());
					String[] list= serverDir.list();
					String listOfFiles= String.join("   ",list);
					dOS.writeUTF(listOfFiles);
					break;
				}
				//mkdir command: to create a directory
				case MKDIR:{
					String makeFolder= dis.readUTF();
					String newFolder= path.toString().concat("/"+makeFolder);
					if(new File(newFolder).exists()){
						dOS.writeUTF("folderExist");
					}
					else{
						File file= new File(newFolder);
						file.mkdir();
						dOS.writeUTF("done");
					}
					break;
				}
				//pwd command: to display path of current working directory
				case PWD:{
					dOS.writeUTF(path.toString());
					break;
				}
				//quit command: to close the connection with server. Bye.. Bye..!!
				case QUIT:{
					clientSocket.close();
					break;
				}
			}
			}while(!str.equalsIgnoreCase(QUIT));
		}catch(Exception e){
			e.printStackTrace();
		}	
		
	}

public void delete(File file, String newPath){
	 String[] list = file.list();
         for(String f : list){
                    if(new File(newPath+"/"+f).isDirectory()){
                   	 delete(new File(newPath+"/"+f), newPath+"/"+f);
                    }else{
                        new File(newPath+"/"+f).delete();
                    }
         }
         file.delete();
   }
}
