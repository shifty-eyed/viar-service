package org.viar.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class HubServer {
	
	private ServerSocket serverSocket;
	private ExecutorService mainLoopRunner;
	private ExecutorService clientPool;

	@PostConstruct
	public void init() throws IOException {
		mainLoopRunner = Executors.newSingleThreadExecutor();
		clientPool = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(2023);
		mainLoopRunner.execute(listeningLoop);
	}
	
	private Runnable listeningLoop = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());
					clientPool.execute(new ClientCommunication(socket));
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Shutting down main listener loop.");
					break;
				}
			}

		}
	};
	
	class ClientCommunication implements Runnable {
		private Socket socket;
		public ClientCommunication(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream());

				clientLoop: while (true) {
					out.println("go");
					out.flush();
					
					while(true) {
						String data = in.readLine();
						if ("close".equals(data)) {
							break clientLoop;
						} else if ("begin".equals(data)) {
							break;
						}
					};
					
					StringBuilder sb = new StringBuilder();
					while(true) {
						String data = in.readLine();
						if ("end".equals(data)) {
							break;
						}
						sb.append(data);
						sb.append("\n");
					};
					
					System.out.println("Received from client: " + sb.toString());
				}

				socket.close();
				System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Client dropped: " + e);
			}
			
		}
		
	}
	
}
