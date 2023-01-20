package org.viar.ui;

import java.awt.Color;
import java.awt.Font;

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
		label.setFont(new Font(Font.DIALOG, Font.BOLD, 30));
		label.setBounds(0, 0, 1200, 500);
		label.setBackground(Color.WHITE);
		label.setEditable(false);
		
		frame.add(label);
		frame.setSize(1200, 500);
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
