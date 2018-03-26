package server;

import java.net.ServerSocket;

public class ServerNPortMediator implements Runnable {
	private ServerSocket nSocket;

	public ServerNPortMediator(ServerSocket nSocket) {
		super();
		this.nSocket = nSocket;
	}

	@Override
	public void run() {
		Thread t = Thread.currentThread();

		while (true) {
			try {
				ServerPortWorker nPortWorker = new ServerNPortWorker(nSocket.accept());
				new Thread(nPortWorker).start();

			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}
}
