package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import prp.MyPrp;

public class DataChannel {
	private Socket dataSocket;
	private DChannelPI dCPI;
	private int connState; // 0:idle 1:transferring 2:closed
	private int command; // 0:STOR
	private String path;
	private DataInputStream input;
	private DataOutputStream output;
	private long size;
	private int blockNO;
	private DataChannel that = this;

	public DataChannel(Socket conn) {
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

	public int getConnState() {
		return connState;
	}

	public void run() throws InterruptedException {
		Thread t = new Thread(dCPI);
		t.start();
		synchronized (this) {
			this.wait();
		}
	}

	public void config(int s, int c, String p, long si) {
		if (connState == 0) {
			connState = s;
			command = c;
			path = p;
			size = si;
		}
	}

	public String config(int s, int c, String p) {
		if (connState == 0) {
			if(c != 3){
				connState = s;
				command = c;
				path = p;
				return "";
			}
			else{
				connState = s;
				command = c;
				String b = p.substring(0, p.indexOf(" "));
				path = p.substring(p.indexOf(" ") + 1);
				blockNO = Integer.parseInt(b);
				return "";
			}
		} else if (connState == 1) {
			return ("ERROR: data channel is busy");
		} else {
			return ("ERROR: data channel is closed");
		}
	}

	private class DChannelPI implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if (command == 0) {
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
					System.out.println("transfer completed");
					synchronized (that) {
						that.notify();
					}
				} else if (command == 1) {
					File file = new File(path);
					if (file.exists() && file.isFile()) {
						file.delete();
					}
					file.createNewFile();
					byte[] buf = new byte[1024];
					FileOutputStream fileOut = new FileOutputStream(file);
					long totalSize = 0;
					while (true) {
						int pos = 0;
						if (input != null) {
							pos = input.read(buf);
							totalSize += 1;
						}
						fileOut.write(buf, 0, pos);
						fileOut.flush();
						if ((pos == -1) || (totalSize == size)) {
							break;
						}
					}
					fileOut.close();
					connState = 0;
					System.out.println("transfer completed");
					synchronized (that) {
						that.notify();
					}
				} else if (command == 2) {
					File file = new File(path);
					if (file.exists() && file.isFile()) {
						file.delete();
					}
					file.createNewFile();
					byte[] buf = new byte[1040];
					long totalSize = 0;
					ArrayList<byte[]> gFile = new ArrayList<byte[]>();
					while (true) {
						int pos = 0;
						if (input != null) {
							pos = input.read(buf);
							totalSize += 1;
							byte[] buf1 = new byte[pos];
							for (int i = 0; i < pos; ++i) {
								buf1[i] = buf[i];
							}
							gFile.add(buf1);
						}
						if ((pos == -1) || (totalSize == size)) {
							break;
						}
					}
					ArrayList<byte[]> hFile = MyPrp.newGetHFromG(gFile);

					FileOutputStream fileOut = new FileOutputStream(file);
					for (int i = 0; i < hFile.size(); ++i) {
						fileOut.write(hFile.get(i));
					}

					fileOut.close();
					connState = 0;
					System.out.println("transfer completed");
					synchronized (that) {
						that.notify();
					}
				} else if(command == 3){
					File f = new File(path);
					int blockLen = 1024;
					int pos;
					int tCount = (int) (f.length() / blockLen);
					int rLen = (int) (f.length() % blockLen);
					if(blockNO == (tCount)){
						FileInputStream fI = new FileInputStream(f);
						fI.skip(1024 * tCount);
						byte[] bufL = new byte[rLen];
						pos = fI.read(bufL);
						fI.close();
						byte[] bufR = new byte[rLen];
						input.read(bufR);
						for(int i = 0;i < rLen;++i){
							if(bufL[i] != bufR[i]){
								System.out.println("verify failed!");
								connState = 0;
								return;
							}
						}
						System.out.println("verify successful");
						connState = 0;
					}
					else{
						FileInputStream fI = new FileInputStream(f);
						fI.skip(1024 * blockNO);
						byte[] bufL = new byte[1024];
						pos = fI.read(bufL);
						fI.close();
						byte[] bufR = new byte[1024];
						input.read(bufR);
						for(int i = 0;i < 1024;++i){
							if(bufL[i] != bufR[i]){
								System.out.println("verify failed!");
								connState = 0;
								synchronized (that) {
									that.notify();
								}
								return;
							}
						}
						System.out.println("verify successful");
						connState = 0;
					}
					synchronized (that) {
						that.notify();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}