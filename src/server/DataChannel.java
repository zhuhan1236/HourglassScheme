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

public class DataChannel {
	private Socket dataSocket;
	private DChannelPI dCPI;
	private String rootPath = CloudServer.serverRoot;
	private int connState; // 0:idle 1:transferring 2:closed
	private int command; // 0:STOR 1:RETR 2:GETH 3:CHAL
	private String path;
	private String contentPath;
	private String indexPath;
	private String pwdPath;
	private String lenPath;
	private DataInputStream input;
	private DataOutputStream output;
	private long size;
	private int blockNO;

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

	public long GETHconfig(int s, int com, String p) throws IOException {
		if (connState == 0) {
			path = p;
			contentPath = rootPath + "content/" + p;
			indexPath = rootPath + "index/" + p;
			pwdPath = rootPath + "pwd/" + p;
			File fp = new File(contentPath);
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
	
	public long RETRconfig(int s, int com, String p) throws IOException {
		if (connState == 0) {
			path = p;
			contentPath = rootPath + "content/" + p;
			indexPath = rootPath + "index/" + p;
			pwdPath = rootPath + "pwd/" + p;
			File fp = new File(contentPath);
			if (fp.exists() && fp.isFile()) {
				connState = s;
				command = com;
				return MyPrp.getLen(p);
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
				lenPath = rootPath + "len/" + p;
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
					while((size - totalSize) >= 1024){
						input.readFully(buf);
						recvFile.add(buf.clone());
						totalSize += 1024;
					}
					if(totalSize != size){
						buf = new byte[(int) (size-totalSize)];
						input.readFully(buf);
						recvFile.add(buf.clone());
					}
					
					try {
						for(int i = 0;i < (recvFile.size() - 1);++i){
							if(recvFile.get(i).length < 1024){
								System.out.println(i + "  sdfds");
							}
						}
						ArrayList<byte[]> gFile = prp.MyPrp
								.enCodeAndWriteToDoc(recvFile,
										prp.MyPrp.myShuffle(size), path);
						
						ArrayList<byte[]> hFile = prp.MyPrp.newGetHFromG(gFile);
								
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
						
						newFile = new File(lenPath);
						if(newFile.exists() && newFile.isFile()){
							newFile.delete();
						}
						newFile.createNewFile();
						FileWriter fw = new FileWriter(newFile);
						fw.write(Long.toString(size));
						fw.close();
						
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
					int blockLen = 1024;
					byte[] hf = new byte[blockLen];
					int pos;
					int tCount = (1040 * (blockNum - 1)) / blockLen;
					int rLen = (1040 * (blockNum - 1)) % blockLen;
					
					int count = 0;
					while(true){
						if(count == tCount){
							byte[] bb = new byte[rLen];
							pos = br.read(bb);
							if(bb.length != 0)
								thFile.add(bb);
						
							byte[] b = new byte[1040];
							pos = br.read(b);
							byte[] tt = new byte[pos];
							for(int i = 0;i < pos;++i){
								tt[i] = b[i];
							}
							if(tt.length != 0)
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
						if(t.length != 0)
							thFile.add(t);
						++count;
					}
					br.close();
					
					if(command == 1){
						ArrayList<byte[]> tgFile = prp.MyPrp.newGetGFromH(thFile);
						ArrayList<byte[]> tfFile = prp.MyPrp.decodeFile(tgFile, path);
						for(int i = 0;i < tfFile.size();++i){
							output.write(tfFile.get(i), 0, tfFile.get(i).length);
							output.flush();
						}
					}else{
						ArrayList<byte[]> tgFile = prp.MyPrp.newGetGFromH(thFile);
						
						for(int i = 0;i < tgFile.size();++i){
							output.write(tgFile.get(i), 0, tgFile.get(i).length);
							output.flush();
						}
					}
					
					connState = 0;
				} else if(command == 3){
					File f = new File(contentPath);
					int blockLen = 1024;
					int pos;
					int tCount = (int) (f.length() / blockLen);
					int rLen = (int) (f.length() % blockLen);
					if(blockNO == (tCount)){
						FileInputStream fI = new FileInputStream(f);
						fI.skip(1024 * tCount);
						byte[] buf = new byte[rLen];
						pos = fI.read(buf);
						output.write(buf, 0, pos);
						output.flush();
						fI.close();
					}
					else{
						FileInputStream fI = new FileInputStream(f);
						fI.skip(1024 * blockNO);
						byte[] buf = new byte[1024];
						pos = fI.read(buf);
						output.write(buf, 0, pos);
						output.flush();
						fI.close();
					}
					connState = 0;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}