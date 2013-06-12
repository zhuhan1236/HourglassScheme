package server;

import java.net.Socket;

public class HourglassHandle extends ConnHandle{
	private HHandlePI handlePI = null;
	private DataChannel dataChannel = null;
	
	public HourglassHandle(Socket conn){
		super(conn);
		handlePI = new HHandlePI();
	}
	
	public HourglassHandle(){
		this(null);
	}
	
	public void run(){
		new Thread(handlePI).start();
	}
	
	private class HHandlePI implements Runnable{

		public void run() {
			DataChannelListener dataChannelListener = new DataChannelListener(4081, 0);
			Thread listenThread = dataChannelListener.run();
			
			synchronized (listenThread) {
				try {
					listenThread.wait();
					dataChannel = dataChannelListener.getDataChannel();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
		}
	}
}