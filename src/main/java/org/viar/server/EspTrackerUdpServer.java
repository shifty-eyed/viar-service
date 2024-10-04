package org.viar.server;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.viar.core.IMUSensorListener;
import org.viar.server.model.EspMessage;

import javax.annotation.PostConstruct;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
public class EspTrackerUdpServer {

	private static final int PORT = 9876;

	private final List<IMUSensorListener> listeners = new ArrayList<>();
	private final Set<InetAddress> clients = new HashSet<>();

	private final IMUDataTestMonitor monitor;
	private final Executor daemonExecutor;
	private volatile boolean running = true;

	private int messageCount = 0;
	private int messagesPerSecond = 0;


	public EspTrackerUdpServer(IMUDataTestMonitor monitor, Executor daemonExecutor) {
		this.monitor = monitor;
		this.daemonExecutor = daemonExecutor;
	}

	@PostConstruct
	public void init() {
		listeners.add(monitor);
		monitor.getButtonSend().addActionListener(e -> sendCommandToClients(monitor.getCommandField()));
		daemonExecutor.execute(this::startUdpServer);
		Runtime.getRuntime().addShutdownHook(new Thread(this::stopUdpServer));
	}


	public void startUdpServer() {
		try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
			System.out.println("UDP Server started on port " + PORT);
			System.out.println(Thread.currentThread().getName());
			System.out.println(Thread.currentThread().isDaemon());

			serverSocket.setSoTimeout(500);

			byte[] receiveBuffer = new byte[1024];  // Adjust buffer size based on message size

			while (running && monitor.isRunning()) {
				DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
				try {
					serverSocket.receive(receivePacket);
				} catch (SocketTimeoutException e) {
					continue;
				}

				processPacket(receivePacket);
				Thread.yield();
			}
			System.out.println("UDP Server stopped");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendCommandToClients(String command) {
		clients.forEach(client -> {
			try (DatagramSocket clientSocket = new DatagramSocket()) {
				byte[] sendData = command.getBytes(StandardCharsets.UTF_8);
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client, PORT);
				clientSocket.send(sendPacket);
				System.out.println("Command sent to client: " + command);
			} catch (Exception e) {
				System.out.println("Error sending command to client: " + e.getMessage());
			}
		});
	}

	public void stopUdpServer() {
		running = false;
		System.out.println("Stopping UDP Server...");
	}

	private void processPacket(DatagramPacket packet) {
		messageCount++;
		clients.add(packet.getAddress());
		try {
			EspMessage message = EspMessage.fromBytes(packet.getData());
			if (message.getType() == EspMessage.TYPE_DATA) {
				updateListeners((EspMessage.Data) message);
			} else {
				System.out.println("Unknown message type: " + message.getType());
			}
		} catch (Exception e) {
			System.out.println("Error parsing message: " + e.getMessage());
		}
	}

	@Scheduled(fixedRate = 1000)
	public void printMessageCount() {
		messagesPerSecond = messageCount;
		messageCount = 0;
	}

	private void updateListeners(EspMessage.Data message) {
		listeners.forEach(listener -> listener.onSensorData(message, messagesPerSecond));
	}


}
