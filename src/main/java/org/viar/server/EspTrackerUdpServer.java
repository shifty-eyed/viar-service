package org.viar.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.viar.core.IMUSensorListener;
import org.viar.server.model.EspMessage;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Component
public class EspTrackerUdpServer {

	private static final int PORT = 9876;

	private final List<IMUSensorListener> listeners = new ArrayList<>();

	private final IMUDataTestMonitor monitor;

	public EspTrackerUdpServer(IMUDataTestMonitor monitor) {
		this.monitor = monitor;
	}

	@PostConstruct
	public void init() {
		listeners.add(monitor);
		startUdpServer();
	}

	@Async
	public void startUdpServer() {
		try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
			System.out.println("UDP Server started on port " + PORT);
			serverSocket.setSoTimeout(500);

			byte[] receiveBuffer = new byte[1024];  // Adjust buffer size based on message size

			while (monitor.isRunning()) {
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

	private void processPacket(DatagramPacket packet) {
		EspMessage message = null;
		try {
			message = EspMessage.fromBytes(packet.getData());
		} catch (Exception e) {
			System.out.println("Error parsing message: " + e.getMessage());
		}

		if (message.getType() == EspMessage.TYPE_DATA) {
			updateListeners((EspMessage.Data) message);
		} else {
			System.out.println("Unknown message type: " + message.getType());
		}

	}

	private void updateListeners(EspMessage.Data message) {
		listeners.forEach(listener -> listener.onSensorData(message));
	}


}
