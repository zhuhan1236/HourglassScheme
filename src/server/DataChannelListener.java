package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DataChannelListener extends Listener {
	private DCListenerPI dCLPI = null;
	private Object dataChannel = null;
	private static ServerSocket server;
	private DataChannelListener that = this;

	public DataChannelListener(int p, int t) {
		super(p, t);
		dCLPI = new DCListenerPI();
	}

	public DataChannelListener(int t) {
		super(t);
		dCLPI = new DCListenerPI();
	}

	public DataChannel getDataChannel() {
		return (DataChannel)dataChannel;
	}
	
	public CheatingDataChannel getCheatingChannel(){
		return (CheatingDataChannel)dataChannel;
	}

	public void waitForDataConnection() {
		synchronized (this) {
			try {
				this.wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		Thread thread = new Thread(dCLPI);
		thread.start();

		synchronized (this) {
			try {
				this.wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class DCListenerPI implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				synchronized (that) {
					try {
						server = new ServerSocket(port);
						that.notify();

					} catch (IOException e) {
						that.notify();
					}

				}
				Socket conn = server.accept();
				if(CloudServer.type == 0){
					dataChannel = new DataChannel(conn);
				}
				else{
					dataChannel = new CheatingDataChannel(conn);
				}
				synchronized (that) {
					that.notify();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}