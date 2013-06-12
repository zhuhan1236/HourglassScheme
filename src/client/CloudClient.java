package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CloudClient{
	public static void main(String[] args) throws UnknownHostException, IOException{
		Socket handleSocket = new Socket("59.66.138.97", 4080);
		Socket dataSocket = new Socket("59.66.138.97", 4081);
		handleSocket.close();
		dataSocket.close();
	}
}