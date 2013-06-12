package server;

import java.net.Socket;

public class HourglassHandle extends ConnHandle{
	private HHandlePI handlePI = null;
	
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

		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}
}