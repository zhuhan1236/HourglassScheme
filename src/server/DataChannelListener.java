package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DataChannelListener extends Listener{
	private DCListenerPI dCLPI = null;
	private DataChannel dataChannel = null;
	
	public DataChannelListener(int p, int t){
		super(p, t);
		dCLPI = new DCListenerPI();
	}
	public DataChannelListener(int t){
		super(t);
		dCLPI = new DCListenerPI();
	}
	
	public DataChannel getDataChannel(){
		return dataChannel;
	}
	
	public Thread run(){
		Thread thread = new Thread(dCLPI);
		thread.start();
		return thread;
	}
	
	private class DCListenerPI implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ServerSocket server;
			try {
				synchronized (this) {
					server = new ServerSocket(port);
					Socket conn = server.accept();
					dataChannel = new DataChannel(conn);
					this.notify();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}