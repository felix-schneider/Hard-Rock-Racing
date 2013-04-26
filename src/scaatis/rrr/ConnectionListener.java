package scaatis.rrr;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener implements Runnable {
	private ServerSocket server;
	private HardRockProtocol protocol;
	private boolean running;

	public ConnectionListener(ServerSocket server, HardRockProtocol protocol) {
		this.server = server;
		this.protocol = protocol;
		this.running = false;
	}

	@Override
	public void run() {
		running = true;
		HardRockProtocol.log(this, "Now listening for incoming connections.");
		while (running) {
			Socket socket;
			try {
				socket = server.accept();
				socket.setSoTimeout(10000);
			} catch (IOException e) {
				return;
			}
			HardRockProtocol.log(this, "Incoming connection from "
					+ socket.getInetAddress().toString());
			Connection connection;
			try {
				connection = new Connection(socket, protocol);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			new Thread(connection).start();
		}
	}
	
	public void stop() {
		running = false;
	}
}
