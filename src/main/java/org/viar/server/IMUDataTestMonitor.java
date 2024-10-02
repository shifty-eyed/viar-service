package org.viar.server;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.viar.core.IMUSensorListener;
import org.viar.server.model.EspMessage;
import org.viar.server.ui.IMUDataPanel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;

@Profile("sensor-test")
@Component
public class IMUDataTestMonitor implements IMUSensorListener {

	@Setter
	@Getter
	private boolean running = true;

	private JFrame window;
	private IMUDataPanel labUI;

	@PostConstruct
	private void init() throws InterruptedException {
		window = new JFrame("Sensor Test");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		labUI = new IMUDataPanel();
		window.setContentPane(labUI.getContentPane());
		window.setVisible(true);
		window.setSize(800, 600);

		labUI.getButtonOK().addActionListener(e -> {
			SwingUtilities.invokeLater(this::shutdown);
		});
	}

	@Override
	public void onSensorData(EspMessage.Data data, int messagesPerSecond) {
		final var numFormat = "%.2f";
		if (running) {
			SwingUtilities.invokeLater(() -> {
				labUI.getAccX().setText(String.format(numFormat,data.getAcc().x));
				labUI.getAccY().setText(String.format(numFormat,data.getAcc().y));
				labUI.getAccZ().setText(String.format(numFormat,data.getAcc().z));

				labUI.getGyroX().setText(String.format(numFormat,data.getGyro().x));
				labUI.getGyroY().setText(String.format(numFormat,data.getGyro().y));
				labUI.getGyroZ().setText(String.format(numFormat,data.getGyro().z));

				labUI.getMagX().setText(String.format(numFormat,data.getMag().x));
				labUI.getMagY().setText(String.format(numFormat,data.getMag().y));
				labUI.getMagZ().setText(String.format(numFormat,data.getMag().z));

				labUI.getAngleX().setText(String.format(numFormat,data.getAngle().x));
				labUI.getAngleY().setText(String.format(numFormat,data.getAngle().y));
				labUI.getAngleZ().setText(String.format(numFormat,data.getAngle().z));

				labUI.getMesageNum().setText(String.valueOf(data.getMessageNumber()));
				labUI.getMsgPerSecond().setText(String.valueOf(messagesPerSecond));

				final String text = String.format("Dev: %d, TS: %d", data.getDeviceId(), data.getTimestamp());
				labUI.getTestArea().setText(text);
			});
		}
	}

	@PreDestroy
	private void shutdown() {
		running = false;
		System.out.println("window.dispose");
	}
}
