package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HandleConnListener extends Listener{
	
	public HandleConnListener(int p, int t){
		super(p, t);
	}
	public HandleConnListener(int t){
		super(t);
	}
	
	public void listen() {
		ServerSocket server;
		try {
			server = new ServerSocket(port);
			while (true) {
				Socket conn = server.accept();
				if(type == 0){
					HourglassHandle hHandle = new HourglassHandle(conn);
					hHandle.run();
				}else{
					CheatingHandle nHandle = new CheatingHandle(conn);
					nHandle.run();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}