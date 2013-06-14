package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;


public class DataChannel{
	private Socket dataSocket;
	private DChannelPI dCPI;
	private int connState; // 0:idle 1:transferring 2:closed
	private int command; // 0:STOR
	private String path;
	private DataInputStream input;
	private DataOutputStream output;
	private long size;
	
	public DataChannel(Socket conn){
		dataSocket = conn;
		connState = 0;
		try {
			input = new DataInputStream(dataSocket.getInputStream());
			output = new DataOutputStream(dataSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dCPI = new DChannelPI();
	}
	
	public int getConnState(){
		return connState;
	}
	
	public void run() {
		new Thread(dCPI).start();
	}
	
	public void config(int s, int c, String p, long si){
		if(connState == 0){
			connState = s;
			command = c;
			path = p;
			size = si;
		}
	}
	
	public String config(int s, int c, String p){
		if(connState == 0){
			connState = s;
			command = c;
			path = p;
			return "";
		}
		else if(connState == 1){
			return ("ERROR: data channel is busy");
		}
		else{
			return ("ERROR: data channel is closed");
		}
	}
	
	private class DChannelPI implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				if(command == 0){
					File file = new File(path);
					FileInputStream fInputStream = new FileInputStream(file);
					byte[] buf = new byte[1024];
					int pos;
					while(true)
					{
						pos = 0;
						if(fInputStream != null)
							pos = fInputStream.read(buf);
						if(pos == -1)
							break;
						output.write(buf, 0, pos);
						output.flush();
					}
					fInputStream.close();
					connState = 0;
					System.out.println("transfer completed");
				}
				else if(command == 1){
					File file = new File(path);
					file.deleteOnExit();
					file.createNewFile();
					byte[] buf = new byte[1024];
					FileOutputStream fileOut = new FileOutputStream(file);
					long totalSize = 0;
					while(true)
					{
						int pos = 0;
						if(input != null)
						{
							pos = input.read(buf);
							totalSize += pos;
						}
						fileOut.write(buf, 0, pos);
						fileOut.flush();
						if((pos == -1) || (totalSize == size))
						{
							break;
						}
					}
					fileOut.close();
					connState = 0;
					System.out.println("transfer completed");
				}
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}