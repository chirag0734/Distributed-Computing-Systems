package participant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;


public class Participant {
	
	
	public static void main(String[] args) {
		Socket participantSocket = null;
		int participantId = 0;
		int coordinatorPort = 0;
		String fileName = null;
		String messageFileName = null;
		String coordinatorIP = null;
		String[] ipAndPort = null;
		try {
			if (args.length == 1 && args[0] != null) {
				fileName = args[0];
				
			} else
				System.out.println("Please provide an argument as filename");
			
			String currentDir = System.getProperty("user.dir");
			File file = new File(currentDir + "/participant/" + fileName);
	 
			if(!file.exists()){
				System.out.println("File doesn't exists");
			}else{
				
	            BufferedReader b = new BufferedReader(new FileReader(file));
	            String readLine = "";

	            participantId = Integer.parseInt(b.readLine());
	            messageFileName = b.readLine();
	            ipAndPort = b.readLine().split(":");
	            String ip = ipAndPort[0];
	            coordinatorPort = Integer.parseInt(ipAndPort[1]);
	            
	            new Thread(new ParticipantAsClient(participantId, messageFileName, ip,coordinatorPort)).start();
	            //participantSocket = new Socket(ip, coordinatorPort);
	           
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
