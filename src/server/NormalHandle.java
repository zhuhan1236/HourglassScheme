package server;

import java.net.Socket;

public class NormalHandle extends ConnHandle{
	private NHandlePI handlePI = null;
	
	public NormalHandle(Socket conn){
		super(conn);
		handlePI = new NHandlePI();
	}
	
	public NormalHandle(){
		this(null);
	}
	
	public void run(){
		new Thread(handlePI).start();
	}
	
	private class NHandlePI implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}