package server;

import java.net.Socket;

public class DataChannel{
	private Socket dataSocket;
	private DChannelPI dCPI;
	
	public DataChannel(Socket conn){
		if((conn != null) && (conn.isConnected())){
			dataSocket = conn;
		}
		dCPI = new DChannelPI();
	}
	
	public void run(){
		new Thread(dCPI).start();
	}
	
	private class DChannelPI implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}