package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataChannel {
	private Socket dataSocket;
	private DChannelPI dCPI;
	private String rootPath = CloudServer.serverRoot;
	private int connState; // 0:idle 1:transferring 2:closed
	private int command; // 0:STOR 1:RETR
	private String path;
	private DataInputStream input;
	private DataOutputStream output;
	private long size;

	public DataChannel(Socket conn) {
		if ((conn != null) && (conn.isConnected())) {
			dataSocket = conn;
		}
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

	public long config(int s, int com, String p) {
		if (connState == 0) {
			path = rootPath + p;
			File fp = new File(path);
			if (fp.exists() && fp.isFile()) {
				connState = s;
				command = com;
				return fp.length();
			} else {
				return -1;
			}
		}
		return -2;
	}

	public String config(int s, int com, String p, long si) {
		if (connState == 0) {
			connState = s;
			size = si;
			command = com;
			path = rootPath + p;
			return "";
		}
		return "ERROR";
	}

	public void run() {
		new Thread(dCPI).start();
	}

	private class DChannelPI implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if (command == 0) {
					File newFile = new File(path);
					if(newFile.exists() && newFile.isFile()){
						newFile.delete();
					}
					newFile.createNewFile();
					byte[] buf = new byte[1024];
					FileOutputStream fileOut = new FileOutputStream(newFile);
					long totalSize = 0;
					while (true) {
						int pos = 0;
						if (input != null) {
							pos = input.read(buf);
							totalSize += pos;
						}
						fileOut.write(buf, 0, pos);
						fileOut.flush();
						if ((pos == -1) || (totalSize == size))
							break;
					}
					fileOut.close();
					connState = 0;
				} else if (command == 1) {
					File file = new File(path);
					FileInputStream fInputStream = new FileInputStream(file);
					byte[] buf = new byte[1024];
					int pos;
					while (true) {
						pos = 0;
						if (fInputStream != null)
							pos = fInputStream.read(buf);
						if (pos == -1)
							break;
						output.write(buf, 0, pos);
						output.flush();
					}
					fInputStream.close();
					connState = 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}