package org.viar.calibration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.viar.calibration.ui.WorldSpaceCalibration;
import org.viar.core.ObjectPositionResolver;

import com.google.gson.Gson;


@Profile("calibration")
@Component
public class HubServer{
	
	private ServerSocket serverSocket;
	private ExecutorService mainLoopRunner;
	private ExecutorService clientPool;
	private Gson gson;
	
	@Autowired
	private ObjectPositionResolver objectPositionResolver;
	
	@Autowired
	private WorldSpaceCalibration monitor;
	
	@PostConstruct
	public void init() throws IOException {
		gson = new Gson();
		mainLoopRunner = Executors.newSingleThreadExecutor();
		clientPool = Executors.newCachedThreadPool();
		serverSocket = new ServerSocket(2023);
		mainLoopRunner.execute(listeningLoop);
	}
	
	private Runnable listeningLoop = new Runnable() {
		@Override
		public void run() {
			while (true) {
				if (monitor.isShutdownTrackers()) {
					System.out.println("Idle..");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
					continue;
				}
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
				List<CameraSpaceFrame> frame = new ArrayList<>();

				clientLoop: while (true) {
					if (monitor.isShutdownTrackers()) {
						out.print("shutdown");
						out.flush();
					} else if (monitor.isTracking()) {
						out.print("go");
						out.flush();
					} else {
						Thread.yield();
						continue;
					}
					
					while(true) {
						String cmd = in.readLine();
						if ("close".equals(cmd)) {
							break clientLoop;
						} else if ("begin".equals(cmd)) {
							break;
						}
					};
					
					while(true) {
						String text = in.readLine();
						if ("end".equals(text)) {
							break;
						}
						frame.add(gson.fromJson(text, CameraSpaceFrame.class));
					};
					
					monitor.show(StringUtils.collectionToCommaDelimitedString(frame));
					frame.clear();
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
