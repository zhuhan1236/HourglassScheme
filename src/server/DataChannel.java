package server;

import java.io.BufferedReader;
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

public class DataChannel {
	private Socket dataSocket;
	private DChannelPI dCPI;
	private String rootPath = CloudServer.serverRoot;
	private int connState; // 0:idle 1:transferring 2:closed
	private int command; // 0:STOR 1:RETR 2:GETH
	private String path;
	private String contentPath;
	private String indexPath;
	private String pwdPath;
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
				return MyPrp.getBlockNum(p);
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
			path = p;
			contentPath = rootPath + "content/" + p;
			indexPath = rootPath + "index/" + p;
			pwdPath = rootPath + "pwd/" + p;
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
						recvFile.add(buf1);
						if ((pos == -1) || (totalSize == size))
							break;
					}
					try {
						ArrayList<byte[]> gFile = prp.MyPrp
								.enCodeAndWriteToDoc(recvFile,
										prp.MyPrp.myShuffle(size), path);
						
						ArrayList<byte[]> hFile = prp.MyPrp.getHdoc(gFile);
						
						File newFile = new File(contentPath);
						if(newFile.exists() && newFile.isFile()){
							newFile.delete();
						}
						newFile.createNewFile();
						FileOutputStream fileOut = new FileOutputStream(newFile);
						for(int i = 0;i < hFile.size();++i){
							fileOut.write(hFile.get(i));
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
				} else if ((command == 1) || (command == 2)) {
					File newFile = new File(contentPath);
					FileInputStream br = new FileInputStream(newFile);
					ArrayList<byte[]> thFile = new ArrayList<byte[]>();
					int blockNum = MyPrp.getBlockNum(path);
					int blockLen = 8 * (blockNum-1);
					byte[] hf = new byte[blockLen];
					int pos;
					int tCount;
					if(blockLen == 0){
						tCount = 0;
					}
					else {
						tCount = (1040 * (blockNum - 1)) / blockLen;
					}
					int count = 0;
					while(true){
						if(count == tCount){
							byte[] b = new byte[1040];
							pos = br.read(b);
							byte[] tt = new byte[pos];
							for(int i = 0;i < pos;++i){
								tt[i] = b[i];
							}
							thFile.add(tt);
							break;
						}
						pos = 0;
						pos = br.read(hf);
						if(pos == -1){
							break;
						}
						byte[] t = new byte[pos];
						for(int i = 0;i < pos;++i){
							t[i] = hf[i];
						}
						thFile.add(t);
						++count;
					}
					br.close();
					
					if(command == 1){
						ArrayList<byte[]> tgFile = prp.MyPrp.getGFormH(thFile);
						ArrayList<byte[]> tfFile = prp.MyPrp.decodeFile(tgFile, path);
						for(int i = 0;i < tfFile.size();++i){
							output.write(tfFile.get(i), 0, tfFile.get(i).length);
							output.flush();
						}
					}else{
						ArrayList<byte[]> tgFile = prp.MyPrp.getGFormH(thFile);
						
						for(int i = 0;i < tgFile.size();++i){
							output.write(tgFile.get(i), 0, tgFile.get(i).length);
							output.flush();
						}
					}
					
					connState = 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}