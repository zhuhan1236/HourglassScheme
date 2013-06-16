package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HourglassHandle extends ConnHandle {
	private HHandlePI handlePI = null;
	private DataChannel dataChannel = null;

	public HourglassHandle(Socket conn) {
		super(conn);
		handlePI = new HHandlePI();
	}

	public HourglassHandle() {
		this(null);
	}

	public void run() {
		new Thread(handlePI).start();
	}

	private CompiledResult STORCommand(CompiledResult com) {
		String size = com.content.substring(0, com.content.indexOf(" "));
		String name = com.content.substring(com.content.indexOf(" ") + 1);
		String confRes = dataChannel.config(1, 0, name, Long.parseLong(size));

		if (confRes.equals("")) {
			dataChannel.run();
			return (new CompiledResult("READY", ""));
		} else {
			return (new CompiledResult("ERROR:", confRes));
		}
	}

	private CompiledResult RETRCommand(CompiledResult com) throws IOException{
		int confRes = dataChannel.config(1, 1, com.content);
		if(confRes == -2){
			return (new CompiledResult("ERROR:", "data channel busy"));
		}
		else if(confRes == -1){
			return (new CompiledResult("ERROR:", "file not exists"));
		}
		else{
			dataChannel.run();
			return (new CompiledResult("READY", Integer.toString(confRes)));
		}
	}
	
	private CompiledResult GETHCommand(CompiledResult com) throws IOException{
		int confRes = dataChannel.config(1, 2, com.content);
		if(confRes == -2){
			return (new CompiledResult("ERROR:", "data channel busy"));
		}
		else if(confRes == -1){
			return (new CompiledResult("ERROR:", "file not exists"));
		}
		else{
			dataChannel.run();
			return (new CompiledResult("READY", Integer.toString(confRes)));
		}
	}
	
	private CompiledResult LISTCommand(CompiledResult com) {
		File file = new File(CloudServer.serverRoot + "content/");
		File[] files = file.listFiles();
		String response = "";
		for (int i = 0; i < files.length; ++i) {
			response += new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
					new Date(files[i].lastModified())).toString();
			response += " ";
			response += Long.toString(files[i].length());
			response += " ";
			response += files[i].getName();
			response += "/";
		}
		
		return (new CompiledResult("", response));
	}

	private CompiledResult execute(CompiledResult com) throws IOException {
		if (com.command.equals("STOR")) {
			return STORCommand(com);
		} else if (com.command.equals("LIST")) {
			return LISTCommand(com);
		}
		else if (com.command.equals("RETR")){
			return RETRCommand(com);
		}
		else if (com.command.equals("GETH")){
			return GETHCommand(com);
		}
		else{
			return (new CompiledResult("UNKNOWN:", com.command));			
		}
	}

	private class CompiledResult {
		private String command = "";
		private String content = "";

		public CompiledResult() {

		}

		public CompiledResult(String com, String con) {
			command = com;
			content = con;
		}

		public void compile(String input) {
			System.out.println(input);
			if(input.contains(" ")){
				command = input.substring(0, input.indexOf(" ")).toUpperCase();
				if(input.trim().equals(command)){
					content = "";
				}
				else{
					content = input.substring(input.indexOf(" ") + 1);
				}
			}
			else{
				command = input.toUpperCase();
				content = "";
			}
		}
	}

	private class HHandlePI implements Runnable {
		public void run() {
			try {
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				DataOutputStream output = new DataOutputStream(
						connection.getOutputStream());

				/*
				 * listen on data channel port
				 */
				DataChannelListener dataChannelListener = new DataChannelListener(
						4081, 0);
				dataChannelListener.run();

				String outputMsg = "PORT 4081\r\n";
				output.write(outputMsg.getBytes());
				
				dataChannelListener.waitForDataConnection();
				
				dataChannel = dataChannelListener.getDataChannel();
				if (dataChannel == null) {
					output.write("TIME OUT! \r\n".getBytes());
					connection.close();
					return;
				}

				String inputMsg = "";
				CompiledResult compiledMsg = new CompiledResult();

				while (true) {
					if (connection.isClosed()) {
						break;
					}

					inputMsg = input.readLine();
					compiledMsg.compile(inputMsg);
					CompiledResult result = execute(compiledMsg);

					if (result == null) {
						if (connection.isClosed()) {
							break;
						} else {
							continue;
						}
					}

					outputMsg = result.command + " " + result.content + "\r\n";
					output.write(outputMsg.getBytes());
					output.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}