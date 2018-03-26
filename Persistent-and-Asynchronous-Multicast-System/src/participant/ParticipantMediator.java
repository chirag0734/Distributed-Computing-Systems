package participant;

import java.net.ServerSocket;

public class ParticipantMediator implements Runnable {
	private ServerSocket nSocket;
	private String messageFile;

	public ParticipantMediator(ServerSocket nSocket, String messageFile) {
		super();
		//System.out.println("In participant mediator constructor");
		this.nSocket = nSocket;
		this.messageFile= messageFile;
	}

	@Override
	public void run() {
		do{
			try {
				while(true){
				ParticipantAsServer participantAsWorker = new ParticipantAsServer(nSocket.accept(), messageFile);
				new Thread(participantAsWorker).start();}

			} catch (Exception e) {
				//System.out.println(e);
			}
		}while(!ParticipantAsClient.flag);
		
	}
}
