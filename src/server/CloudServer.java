package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CloudServer{
	private int port;
	public static int type;
	public static String serverRoot = "/home/zh-pc/secFile/";
	
	public CloudServer(){
		this(4080, 1);
	}
	
	public CloudServer(int p){
		this(p, 1);
	}
	
	public CloudServer(int p, int t){
		port = p;
		type = t;
	}
	
	public static void main(String[] args) throws IOException{
		File f = new File("serverConfig.txt");
		FileReader fr = new FileReader(f);
		BufferedReader fb = new BufferedReader(fr);
		serverRoot = fb.readLine();
		File d = new File(serverRoot + "content");
		if((!d.exists()) || (!d.isDirectory())){
			d.mkdir();
		}
		d = new File(serverRoot + "pwd");
		if((!d.exists()) || (!d.isDirectory())){
			d.mkdir();
		}
		d = new File(serverRoot + "index");
		if((!d.exists()) || (!d.isDirectory())){
			d.mkdir();
		}
		CloudServer cloudServer = new CloudServer(Integer.parseInt(fb.readLine()), Integer.parseInt(fb.readLine()));
		cloudServer.startListen();
		fb.close();
		fr.close();
		
		/*if(args.length == 0){
			CloudServer cloudServer = new CloudServer();
			cloudServer.startListen();	
		}
		else if(args.length == 1){
			CloudServer cloudServer = new CloudServer(Integer.parseInt(args[0]));
			cloudServer.startListen();
		}
		else if(args.length == 2){
			CloudServer cloudServer = new CloudServer(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			cloudServer.startListen();
		}
		else{
			System.out.println("args error");
		}*/
	}
	
	public void startListen() throws IOException{
		HandleConnListener hConnListener = new HandleConnListener(port, type);
		hConnListener.listen();
	}
}