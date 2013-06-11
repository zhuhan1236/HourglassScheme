package server;

import java.net.Socket;

public abstract class ConnHandle{
	protected Socket connection;
	
	public ConnHandle(){
		
	}
	
	public ConnHandle(Socket conn){
		if((conn != null) && (conn.isConnected())){
			connection = conn;
		}
		else{
			System.out.println("socket not connected!");
		}
	}
}