package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import prp.MyPrp;

public class CheatingDataChannel {
	private Socket dataSocket;
	private DChannelPI dCPI;
	private String rootPath = CloudServer.serverRoot;
	private int connState; // 0:idle 1:transferring 2:closed
	private int command; // 0:STOR 1:RETR 2:GETH 3:CHAL
	private String path;
	private String contentPath;
	private String indexPath;
	private String pwdPath;
	private DataInputStream input;
	private DataOutputStream output;
	private long size;
	private int blockNO;

	public CheatingDataChannel(Socket conn) {
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
	
	public int config(int s, int com, String p) throws IOException {
		if (connState == 0) {
			path = p;
			contentPath = rootPath + "content/" + p;
			indexPath = rootPath + "index/" + p;
			pwdPath = rootPath + "pwd/" + p;
			File fp = new File(contentPath);
			if (fp.exists() && fp.isFile()) {
				connState = s;
				command = com;
				return (int) (fp.length() / 1024 + 1);
			} else {
				return -1;
			}
		}
		return -2;
	}

	public String config(int s, int com, String p, long si) {
		if (connState == 0) {
			if(com != 3){
				connState = s;
				size = si;
				command = com;
				path = p;
				contentPath = rootPath + "content/" + p;
				indexPath = rootPath + "index/" + p;
				pwdPath = rootPath + "pwd/" + p;
				return "";
			}
			else{
				connState = s;
				blockNO = (int) si;
				command = com;
				path = p;
				contentPath = rootPath + "content/" + p;
				indexPath = rootPath + "index/" + p;
				pwdPath = rootPath + "pwd/" + p;
				return "";
			}
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
					ArrayList<byte[]> recvFile = new ArrayList<byte[]>();
					byte[] buf = new byte[1024];
					long totalSize = 0;
					while (true) {
						int pos = 0;
						if (input != null) {
							pos = input.read(buf);
							totalSize += pos;
						}
						byte[] buf1 = new byte[pos];
						for(int i = 0;i < pos;++i){
							buf1[i] = buf[i];
						}
						if(buf1.length != 0)
							recvFile.add(buf1);
						if ((pos == -1) || (totalSize == size))
							break;
					}
					try {
						ArrayList<byte[]> gFile = prp.MyPrp
								.enCodeAndWriteToDoc(recvFile,
										prp.MyPrp.myShuffle(size), path);
								
						File newFile = new File(contentPath);
						if(newFile.exists() && newFile.isFile()){
							newFile.delete();
						}
						FileOutputStream fileOut = new FileOutputStream(newFile);
						for(int i = 0;i < gFile.size();++i){
							fileOut.write(gFile.get(i), 0, gFile.get(i).length);
						}
						fileOut.close();
						connState = 0;
						
					} catch (InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalBlockSizeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BadPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if(command == 1){
					File file = new File(contentPath);
					FileInputStream fInputStream = new FileInputStream(file);
					ArrayList<byte[]> gFile = new ArrayList<byte[]>();
					byte[] buf = new byte[1040];
					int pos;
					while (true) {
						pos = 0;
						if (fInputStream != null)
							pos = fInputStream.read(buf);
						if (pos == -1)
							break;
						byte[] buf1 = new byte[pos];
						for(int i = 0;i < pos;++i){
							buf1[i] = buf[i];
						}
						gFile.add(buf1);
					}
					fInputStream.close();
					ArrayList<byte[]> fFile = MyPrp.decodeFile(gFile, path);
					for(int i = 0;i < fFile.size();++i){
						output.write(fFile.get(i), 0, fFile.get(i).length);
					}
					connState = 0;
				}
				else if (command == 2) {
					byte[] buf = new byte[1040];
					File file = new File(contentPath);
					FileInputStream fI = new FileInputStream(file);
					while (true) {
						int pos = 0;
						pos = fI.read(buf);
						if ((pos == -1))
							break;
						output.write(buf, 0, pos);
					}
					fI.close();
					connState = 0;
				} else if(command == 3){
					File file = new File(contentPath);
					FileInputStream fInputStream = new FileInputStream(file);
					ArrayList<byte[]> gFile = new ArrayList<byte[]>();
					byte[] buf = new byte[1040];
					int pos;
					while (true) {
						pos = 0;
						if (fInputStream != null)
							pos = fInputStream.read(buf);
						if (pos == -1)
							break;
						byte[] buf1 = new byte[pos];
						for(int i = 0;i < pos;++i){
							buf1[i] = buf[i];
						}
						if(pos != 0)
							gFile.add(buf1);
					}
					fInputStream.close();
					
					ArrayList<byte[]> hFile = MyPrp.newGetHFromG(gFile);
					if((hFile.get(blockNO).length < 1024) && ((hFile.size() - 1) > blockNO)){
						int lastTwo = hFile.get(hFile.size() - 1).length + hFile.get(hFile.size() - 2).length;
						if(lastTwo < 1024){
							byte[] b = new byte[lastTwo];
							int i = 0;
							for(;i < hFile.get(hFile.size() - 2).length;++i){
								b[i] = hFile.get(hFile.size() - 2)[i];
							}
							for(int j = lastTwo - 1;j >= i;--j){
								b[j] = hFile.get(hFile.size() - 1)[j - i];
							}
							output.write(b, 0, lastTwo);
						}
						else{
							byte[] b = new byte[1024];
							int i = 0;
							for(;i < hFile.get(hFile.size() - 2).length;++i){
								b[i] = hFile.get(hFile.size() - 2)[i];
							}
							for(int j = 1024 - 1;j >= i;--j){
								b[j] = hFile.get(hFile.size() - 1)[j - i];
							}
							output.write(b, 0, 1024);
						}
					}
					else{
						output.write(hFile.get(blockNO), 0, hFile.get(blockNO).length);
						File nnewFile = new File("/home/zh-pc/secFile/content/h.txt");
						if(nnewFile.exists())
							nnewFile.delete();
						nnewFile.createNewFile();
						FileOutputStream ffileOut = new FileOutputStream(nnewFile);
						
					
						for(int i = 0;i < hFile.size();++i){
							ffileOut.write(hFile.get(i), 0, gFile.get(i).length);
						}
						
						ffileOut.close();
					}
					
					connState = 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}