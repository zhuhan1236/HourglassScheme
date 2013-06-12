package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DataChannelListener extends Listener{
	public DataChannelListener(int p, int t){
		super(p, t);
	}
	public DataChannelListener(int t){
		super(t);
	}
	
	public DataChannel run(){
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			Socket conn = server.accept();
			DataChannel dataChannel = new DataChannel(conn);
			return dataChannel;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}