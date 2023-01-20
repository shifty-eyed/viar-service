package org.viar.ui;

import java.awt.Color;

import javax.annotation.PostConstruct;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.springframework.stereotype.Component;

@Component
public class Monitor {

	private JFrame frame;
	private JTextArea label;

	@PostConstruct
	private void init() {
		frame = new JFrame("Camera calibration");
		frame.setLayout(null);
		
		label = new JTextArea("no data");
		label.setBounds(0, 0, 800, 200);
		label.setBackground(Color.WHITE);
		label.setEditable(false);
		
		frame.add(label);
		frame.setSize(810, 210);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}

	public void onChange(String data, long timeMillis) {
		SwingUtilities.invokeLater(() -> {
			frame.setTitle("Camera calibration. Time: "+timeMillis);
			label.setText(data);
		});

	}

}
