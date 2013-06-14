package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class ConnHandle {
	private Socket connection;
	private BufferedReader input;
	private DataOutputStream output;
	private String inputMsg;
	private String outputMsg;
	private CompiledResult compiledMsg;
	private DataChannel dataChannel;

	public ConnHandle(Socket conn) {
		connection = conn;
		try {
			input = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			output = new DataOutputStream(connection.getOutputStream());
			compiledMsg = new CompiledResult();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			if (input.contains(" ")) {
				command = input.substring(0, input.indexOf(" ")).toUpperCase();
				if (input.trim().equals(command)) {
					content = "";
				} else {
					content = input.substring(input.indexOf(" ") + 1);
				}
			} else {
				command = input.toUpperCase();
				content = "";
			}
		}
	}

	private void handleLIST(String response) {
		System.out.println("cloud server has the following files >");
		response = response.trim();
		String files[] = response.split("/");
		for (int i = 0; i < files.length; ++i) {
			System.out.println(files[i]);
		}
	}

	private void handleSTOR(String response) {
		CompiledResult r = new CompiledResult();
		r.compile(response);
		if (r.command.startsWith("ERROR")) {
			System.out.println(response);
		} else {
			String confRes = dataChannel.config(1, 0, compiledMsg.content);
			if (confRes.equals("")) {
				dataChannel.run();
				System.out.println("transfering...");
			} else {
				System.out.println(confRes);
			}
		}
	}

	private void handleRETR(String response) {
		CompiledResult r = new CompiledResult();
		r.compile(response);
		if (r.command.startsWith("ERROR")) {
			System.out.println(response);
		} else {
			dataChannel.config(1, 1, CloudClient.workspace
					+ compiledMsg.content, Long.parseLong(response
					.substring(response.indexOf(" ") + 1)));
			System.out.println("transfering...");
			dataChannel.run();
		}
	}

	private void waitResponse(CompiledResult msg) {
		try {
			if (msg.command.equals("LIST")) {
				outputMsg = compiledMsg.command + " " + compiledMsg.content
						+ "\r\n";
				output.write(outputMsg.getBytes());
				inputMsg = input.readLine();
				handleLIST(inputMsg);
			} else if (msg.command.equals("STOR")) {
				String filePath = compiledMsg.content;
				File fp = new File(filePath);
				if (fp.exists() && fp.isFile()) {
					long size = fp.length();
					outputMsg = compiledMsg.command + " " + Long.toString(size)
							+ " " + fp.getName() + "\r\n";
					output.write(outputMsg.getBytes());
					inputMsg = input.readLine();
					handleSTOR(inputMsg);
				} else {
					System.out.println("ERROR: local file not exist");
				}
			} else if (msg.command.equals("RETR")) {
				if(dataChannel.getConnState() != 0){
					System.out.println("data channel is busy");
					return;
				}
				outputMsg = compiledMsg.command + " " + compiledMsg.content
						+ "\r\n";
				output.write(outputMsg.getBytes());
				inputMsg = input.readLine();
				handleRETR(inputMsg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		try {
			inputMsg = input.readLine();
			compiledMsg.compile(inputMsg);
			Socket dataConn = new Socket(CloudClient.ipAddress,
					Integer.parseInt(compiledMsg.content));

			dataChannel = new DataChannel(dataConn);

			Scanner s = new Scanner(System.in);

			while (true) {
				inputMsg = s.nextLine();
				compiledMsg.compile(inputMsg);
				waitResponse(compiledMsg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}