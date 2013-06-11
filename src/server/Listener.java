package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener {
	public static void main(String[] args) {
		Listener listener = new Listener();
		listener.listen(0);
	}

	public void listen(int type) {
		ServerSocket server;
		try {
			server = new ServerSocket(4080);
			while (true) {
				Socket conn = server.accept();
				if(type == 0){
					HourglassHandle hHandle = new HourglassHandle(conn);
				}else{
					NormalHandle nHandle = new NormalHandle(conn);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}