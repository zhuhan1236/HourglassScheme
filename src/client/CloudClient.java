package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class CloudClient{
	public static String ipAddress;
	public static String workspace = "/home/zh-pc/secLocal/";
	public static int myPort;
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException{
		File f = new File("clientConfig.txt");
		FileReader fr = new FileReader(f);
		BufferedReader fb = new BufferedReader(fr);
		workspace = fb.readLine();
		ipAddress = fb.readLine();
		myPort = Integer.parseInt(fb.readLine());
		
		Socket handleSocket = null;
		try{
			handleSocket = new Socket(ipAddress, myPort);
		}
		catch (IOException e){
			System.out.println("connect failed, change config");
			return;
		}
		ConnHandle connHandle = new ConnHandle(handleSocket);
		connHandle.run();
	}
}