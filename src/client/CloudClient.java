package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class CloudClient{
	public static String ipAddress;
	public static String workspace = "/home/zh-pc/secLocal/";
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		System.out.println("input server IP >");
		Scanner s = new Scanner(System.in);
		
		
		Socket handleSocket;
		while(true){
			try{
				ipAddress = s.nextLine();
				handleSocket = new Socket(ipAddress, 4080);
				break;
			}
			catch (IOException e){
				System.out.println("connect failed, try again");
				continue;
			}
		}
		ConnHandle connHandle = new ConnHandle(handleSocket);
		connHandle.run();
	}
}