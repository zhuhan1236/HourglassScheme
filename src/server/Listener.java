package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Listener {
	protected int port;
	protected int type;
	
	public Listener(int p, int t){
		port = p;
		type = t;
	}
	
	public Listener(int t){
		this(4080, t);
	}
	
	public Listener(){
		this(4080, 1);
	}
}