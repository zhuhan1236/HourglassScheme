package prp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import server.CloudServer;

public class MyPrp {
	// shuffle
	public static ArrayList<Integer> myShuffle(long docSize) {
		ArrayList<Integer> myList = new ArrayList<Integer>();
		int i;
		long docBlockNum = docSize / 1024;
		if (docSize % 1024 != 0)
			docBlockNum += 1;
		for (i = 0; i < docBlockNum-1; i++)
			myList.add(i);
		Collections.shuffle(myList);
		myList.add((int)docBlockNum-1);
		return myList;
	}

	public static String getRandomPassword(int length) {
		final int maxLength = 50;
		final int maxNum = 36;
		Date md = new Date();
		char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
				'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
				'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		if (length > maxLength) {
			return null;
		}
		int count;
		Random r = new Random(md.getTime());
		StringBuffer password = new StringBuffer();
		for (count = 0; count < length; count++)
			password.append(str[Math.abs(r.nextInt(maxNum))]);
		return password.toString();
	}

	public static ArrayList<byte[]> enCodeAndWriteToDoc(ArrayList<byte[]> content,
			ArrayList<Integer> myList, String path) throws IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		int i;
		final int pwdLength = 16;
		String password;
		File pwdFile = new File(server.CloudServer.serverRoot + "pwd/" + path);
		File indexFile = new File(server.CloudServer.serverRoot + "index/" + path);
		ArrayList<byte[]> result = new ArrayList<byte[]>();
		SecretKeySpec key;
		if (!pwdFile.exists()) {
			pwdFile.createNewFile();
		}
		if (!indexFile.exists()) {
			indexFile.createNewFile();
		}
		FileWriter fo = new FileWriter(pwdFile);
		FileWriter nfo = new FileWriter(indexFile);
		int index;
		// in this function, I need generate keys for every block
		for (i = 0; i < myList.size(); i++) {
			index = myList.indexOf(i);
			password = getRandomPassword(pwdLength);
			// byte[] enCodeFormat = secretKey.getEncoded();
			key = new SecretKeySpec(password.getBytes(), "AES");
			// key has generated,the key is about the shuffled set,and I need to
			// write the key to the file
			fo.write(password + "\r\n");
			nfo.write(myList.get(i).toString() + "\r\n");
			// md5Key = MyMD5.getMD5(key.toString().getBytes()).getBytes();
			// md5Index =
			// MyMD5.getMD5(myList.get(i).toString().getBytes()).getBytes();
			// result = new byte[md5Key.length];
			// for(int j = 0;j < md5Key.length;j ++){
			// result[j] = (byte)(md5Key[j] ^ md5Index[j]);
			// }
			result.add(encrypt(new String(content.get(myList.get(index))), key));
		}
		fo.close();
		nfo.close();
		return result;
	}

	public static ArrayList<byte[]> decodeFile(ArrayList<byte[]> gFile, String path) throws IOException {
		File pwdFile = new File(server.CloudServer.serverRoot + "pwd/" + path);
		File indexFile = new File(server.CloudServer.serverRoot + "index/" + path);
		FileReader pwdIS = new FileReader(pwdFile);
		BufferedReader pwdBR = new BufferedReader(pwdIS);
		FileReader indexIS = new FileReader(indexFile);
		BufferedReader indexBR = new BufferedReader(indexIS);
		ArrayList<String> keyString = new ArrayList<String>();
		ArrayList<String> rString = new ArrayList<String>();
		ArrayList<byte[]> returnByte = new ArrayList<byte[]>();
		String indexString = null;
		SecretKeySpec key;
		String line = null;
		int i = 0;
		while ((line = pwdBR.readLine()) != null) {
			keyString.add(line);
			i++;
		}
		pwdBR.close();

		int index = -1;
		for (int k = 0; k < i; k++) {
			indexString = indexBR.readLine();
			index = Integer.parseInt(indexString);
			key = new SecretKeySpec(keyString.get(k).getBytes(), "AES");
			returnByte.add(decrypt(gFile.get(index), key));
		}
		indexBR.close();

		return returnByte;
	}

	public static byte[] encrypt(String content, SecretKeySpec password)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			UnsupportedEncodingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		byte[] result;
		try {
			Cipher cipher = Cipher.getInstance("AES");
			byte[] byteContent = content.getBytes();
			cipher.init(Cipher.ENCRYPT_MODE, password);
			result = cipher.doFinal(byteContent);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decrypt(byte[] content, SecretKeySpec password) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, password);
			byte[] result = cipher.doFinal(content);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}
	
	public static ArrayList<byte[]> getHdoc(ArrayList<byte[]> gDoc) {
		ArrayList<byte[]> returnBytes = new ArrayList<byte[]>();
		byte[] buffer;
		int i, j, k;
		if (gDoc.size() == 1){
			returnBytes.add(gDoc.get(gDoc.size()-1));
			return returnBytes;
		}
		for (i = 0; i < gDoc.get(0).length / 8; i++) {
			buffer = new byte[gDoc.size()*8-8];
			for (j = 0; j < gDoc.size() - 1; j++) {
				for (k = 0; k < 8; k++) {
					buffer[j*8 + k] = gDoc.get(j)[i * 8 + k];
				}
			}
			returnBytes.add(buffer);
		}
		returnBytes.add(gDoc.get(gDoc.size()-1));
		return returnBytes;
	}
	
	public static ArrayList<byte[]> newGetHFromG(ArrayList<byte[]> gDoc){
		ArrayList<byte[]> returnBytes = new ArrayList<byte[]>();
		final int hBlockSize = 1024;
		int i,length,j,k;
		//j is column,8 bytes is a column
		byte[] buffer;
		if (gDoc.size() == 1){
			returnBytes.add(gDoc.get(gDoc.size()-1));
			return returnBytes;
		}
		
		buffer = new byte[hBlockSize];
		length = 0;
		for (j = 0;j < gDoc.get(0).length/8;j++){
			for (i = 0;i < gDoc.size()-1;i++){
				if (length == hBlockSize){
					returnBytes.add(buffer);
					length = 0;
					buffer = new byte[hBlockSize];
				}
				for (k = 0;k < 8;k++){
					buffer[length] = gDoc.get(i)[k+j*8];
					length++;
				}
			}
		}
		if (length != hBlockSize){
			byte[] newBuffer = new byte[length];
			for (i = 0;i<length;i++){
				newBuffer[i] = buffer[i];
			}
			//returnBytes.remove(returnBytes.size()-1);
			returnBytes.add(newBuffer);
		}
		returnBytes.add(gDoc.get(gDoc.size()-1));
		return returnBytes;
	}
	
	public static ArrayList<byte[]> newGetGFromH(ArrayList<byte[]> hDoc){
		ArrayList<byte[]> returnBytes = new ArrayList<byte[]>();
		final int gBlockSize = 1040;
		final int littleInHBlock = 1024 / 8;
		int byteNum = 0;
		for (int m = 0;m < hDoc.size();m++){
			byteNum += hDoc.get(m).length;
		}
		int gBlockNum = byteNum / 1040;
		int littleBlock = 0;
		int b;
		int bB;
		if (hDoc.size() == 1){
			returnBytes.add(hDoc.get(hDoc.size()-1));
			return returnBytes;
		}
		int wholeLittleBlocks  = littleInHBlock * (hDoc.size() - 2);
		wholeLittleBlocks += hDoc.get(hDoc.size() - 2).length / 8;
		int length = 0;
		byte[] buffer = new byte[gBlockSize];
		for (int n = 0;n < gBlockNum;n++){
			buffer = new byte[gBlockSize];
			length = 0;
			for (littleBlock = n;littleBlock < wholeLittleBlocks;littleBlock+= gBlockNum){

				b = littleBlock % littleInHBlock;
				bB = littleBlock / littleInHBlock;
				
				for (int k = 0;k < 8;k ++){
					buffer[length] = hDoc.get(bB)[b*8 + k];
					length ++;
				}
			}
			returnBytes.add(buffer);
		}
		returnBytes.add(hDoc.get(hDoc.size()-1));
		return returnBytes;
	}
	
	
	public static ArrayList<byte[]> getGFormH(ArrayList<byte[]> hDoc){
		ArrayList<byte[]> returnBytes = new ArrayList<byte[]>();
		int i,j,k;
		byte[] buffer;
		if (hDoc.size() == 1){
			returnBytes.add(hDoc.get(hDoc.size()-1));
			return returnBytes;
		}
		for (i = 0;i < hDoc.get(0).length / 8;i++){
			buffer = new byte[1040];
			for (j = 0; j < hDoc.size()-1;j++){
				for (k = 0;k < 8;k ++){
					buffer[k + j*8] = hDoc.get(j)[i*8+k];
				}
			}
			returnBytes.add(buffer);
		}
		returnBytes.add(hDoc.get(hDoc.size()-1));
		return returnBytes;
	}
	
	public static boolean integrityChecking(byte[] a, byte[] b) {
		String aMD5 = MyMD5.getMD5(a);
		String bMD5 = MyMD5.getMD5(b);
		return aMD5.equals(bMD5);
	}

	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	public static int getBlockNum(String path) throws IOException{
		String pwdPath = CloudServer.serverRoot + "pwd/" + path;
		
		File fp = new File(pwdPath);
		FileReader fr = new FileReader(fp);
		BufferedReader br = new BufferedReader(fr);
		int line = 0;
		while(br.readLine() != null){
			++line;
		}
		br.close();
		return (line);
	}
}
