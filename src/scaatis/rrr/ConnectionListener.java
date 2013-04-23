package scaatis.rrr;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener implements Runnable {
	private ServerSocket server;
	private HardRockProtocol protocol;

	public ConnectionListener(ServerSocket server, HardRockProtocol protocol) {
		this.server = server;
		this.protocol = protocol;
	}

	@Override
	public void run() {
		HardRockProtocol.log(this, "Now listening for incoming connections.");
		while (true) {
			Socket socket;
			try {
				socket = server.accept();
				socket.setSoTimeout(10000);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			HardRockProtocol.log(this, "Incoming connection from "
					+ socket.getInetAddress().toString());
			Connection connection;
			try {
				connection = new Connection(socket);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			new Thread(new Handshake(connection, protocol)).start();
		}
	}
}
