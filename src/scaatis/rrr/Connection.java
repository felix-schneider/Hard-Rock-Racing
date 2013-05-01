package scaatis.rrr;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;

public class Connection implements Closeable, Runnable {
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private HardRockProtocol protocol;
	private ConcurrentLinkedQueue<String> inputQueue;
	private ConcurrentLinkedQueue<String> outputQueue;
	private boolean running;

	public Connection(Socket socket, HardRockProtocol protocol)
			throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.protocol = protocol;
		running = false;
		inputQueue = new ConcurrentLinkedQueue<>();
		outputQueue = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void run() {
		running = true;
		// Handshake
		HardRockProtocol.log(this, "Waiting for handshake...");
		String line = null;
		try {
			line = in.readLine();
		} catch (SocketTimeoutException e) {
			HardRockProtocol.log(this, socket.getInetAddress().toString()
					+ " did not send handshake.");
			close();
			return;
		} catch (IOException e) {
			HardRockProtocol.log(this,
					"I/O error while reading connection from "
							+ socket.getInetAddress().toString());
			close();
			return;
		}
		if (line == null) {
			HardRockProtocol.log(this, "Connection to "
					+ socket.getInetAddress().toString()
					+ " was remotely closed.");
			close();
			return;
		}

		Player player = protocol.attemptHandShake(line, out);
		if (player == null) {
			HardRockProtocol
					.log(this, "Handshake from "
							+ socket.getInetAddress().toString()
							+ " was unsuccessful.");
		} else {
			protocol.connectPlayer(this, player);
		}
		try {
            socket.setSoTimeout(10);
        } catch (SocketException e1) {
            close();
        }
		while (running) {
		    while(!outputQueue.isEmpty()) {
		        out.println(outputQueue.poll());
		    }
			try {
				line = in.readLine();
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				HardRockProtocol.log(this, "Connection to "
						+ socket.getInetAddress().toString()
						+ " ended unexpectedly.");
				protocol.connectionLost(this);
				close();
				break;
			}
			if (line == null) {
				HardRockProtocol.log(this, "Connection to "
						+ socket.getInetAddress().toString()
						+ " was remotely closed.");
				protocol.connectionLost(this);
				close();
				break;
			}
			inputQueue.add(line);
		}
	}

	@Override
	public void close() {
		stop();
		out.close();
		try {
			in.close();
			socket.close();
		} catch (IOException e) {
			// like I give a fuck
		}
	}

	public void stop() {
		running = false;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Connection)) {
			return false;
		}
		return socket == ((Connection) other).socket;
	}

	@Override
	public int hashCode() {
		return socket.hashCode();
	}

	public InetAddress getAddress() {
		return socket.getInetAddress();
	}

	public boolean hasInput() {
		return !inputQueue.isEmpty();
	}

	public String nextInput() {
		return inputQueue.poll();
	}
	
	public void send(String message) {
	    if(outputQueue.size() > 5) {
	        outputQueue.poll();
	    }
	    outputQueue.add(message);
	}
	
	public void send(JSONObject message) {
		send(message.toString());
	}
	
	public void send(JSONable message) {
		send(message.toJSON());
	}
}
