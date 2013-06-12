package server;

public class CloudServer{
	private int port;
	private int type;
	
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
	
	public static void main(String[] args){
		if(args.length == 0){
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
		}
	}
	
	public void startListen(){
	}
}