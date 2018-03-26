package server;

import java.net.ServerSocket;

public class ServerTPortMediator implements Runnable {
	private ServerSocket tSocket;
	private int nPort;
	private int tPort;

	public ServerTPortMediator(ServerSocket tSocket) {
		super();
		this.tSocket = tSocket;
	}

	@Override
	public void run() {
		Thread t = Thread.currentThread();
		String name = t.getName();

		while (true) {
			try {
				ServerPortWorker tPortWorker = new ServerTPortWorker(tSocket.accept());

				new Thread(tPortWorker).start();

			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}
}
