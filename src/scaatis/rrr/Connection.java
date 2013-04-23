package scaatis.rrr;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Closeable {
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	public Connection(Socket socket) throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	@Override
	public void close() throws IOException {
		out.close();
		in.close();
		socket.close();
	}

	public String getLine() throws IOException {
		return in.readLine();
	}

	public void println(String line) {
		out.println(line);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Connection)) {
			return false;
		}
		return socket.equals(((Connection) other).socket);
	}

	@Override
	public int hashCode() {
		return socket.hashCode();
	}
	
	public Socket getSocket() {
		return socket;
	}
}
