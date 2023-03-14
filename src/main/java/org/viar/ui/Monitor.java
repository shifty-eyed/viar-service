package org.viar.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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
		
		frame.setFocusTraversalKeysEnabled(false);
		frame.setFocusable(true);
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				System.out.println(label.getText());
			}
			
		});
		
		label = new JTextArea("no data");
		label.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		label.setBounds(0, 0, 1800, 800);
		label.setBackground(Color.WHITE);
		label.setEditable(false);
		
		frame.add(label);
		frame.setSize(1800, 800);
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
