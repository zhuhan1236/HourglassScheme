package server;

import java.net.Socket;

public class CheatingHandle extends ConnHandle{
	private CHandlePI handlePI = null;
	
	public CheatingHandle(Socket conn){
		super(conn);
		handlePI = new CHandlePI();
	}
	
	public CheatingHandle(){
		this(null);
	}
	
	public void run(){
		new Thread(handlePI).start();
	}
	
	private class CHandlePI implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}