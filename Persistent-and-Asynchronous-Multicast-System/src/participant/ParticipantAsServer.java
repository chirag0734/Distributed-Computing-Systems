package participant;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

public class ParticipantAsServer implements Runnable {

	//ServerSocket participantServerSocket;
	Socket clientSocket;
	int receivingPort;
	String messageFile;

	public ParticipantAsServer(Socket clientSocket, String messageFile) {

		super();
		this.clientSocket = clientSocket;
		this.messageFile = messageFile;
	}

	@Override
	public void run() {

		try {

			//do{
				// System.out.println("Particpant Sever Started");
			//if(!ParticipantAsClient.flag){
				DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());
				String msg = dIS.readUTF();
				System.out.println("Message Received::" + msg);
				BufferedWriter out = null;
				try {
					String currentDir = System.getProperty("user.dir");
					File file = new File(currentDir + "/participant/" + messageFile);
					FileWriter fstream = new FileWriter(file, true);
					out = new BufferedWriter(fstream);
					out.write(msg);
					out.newLine();
				} catch (IOException e) {
					e.printStackTrace();

				} finally {
					if (out != null) {
						out.close();
					}
				}
			//}while(!ParticipantAsClient.flag);
			
			/*try{
				clientSocket.close();
			}catch(Exception e){
				e.printStackTrace();
			}*/

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

}