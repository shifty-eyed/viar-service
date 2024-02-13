package org.viar.calibration.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.calibration.CalibrationDataCollector;
import org.viar.calibration.WorldCoordinatesPresets;
import org.viar.core.TrackingListener;
import org.viar.core.model.CameraSpaceFrame;
import org.viar.core.model.WorldSpaceVertex;

@Component
public class CalibrationEditor implements TrackingListener {

	private JFrame frame;
	private JTextArea label;
	private JComboBox<String> presetSelect;
	private JButton doCapture;
	private JButton doSave;
	private JButton doLoadAndCalibrate;
	private JCheckBox chkStartStopTracking;
	private JCheckBox chkShutdownTrackers;
	private JTextField tfFileName;
	
	@Autowired
	private WorldCoordinatesPresets coordProvider;
	
	@Autowired
	private CalibrationDataCollector dataCollector;
	
	private Collection<CameraSpaceFrame> cameraSpaceSamples;

	@PostConstruct
	private void init() throws Exception {
		 UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
		
		frame = new JFrame("Camera calibration");
		frame.setLayout(null);
		
		frame.setFocusTraversalKeysEnabled(false);
		frame.setFocusable(true);
		
		label = new JTextArea("no data");
		label.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
		label.setBounds(0, 40, 1800, 760);
		label.setBackground(Color.WHITE);
		label.setEditable(false);
		frame.add(label);
		
		presetSelect = new JComboBox<>(coordProvider.getPresetNames());
		presetSelect.setBounds(10, 2, 150, 25);
		frame.add(presetSelect);
		
		doCapture = new JButton("Capture");
		doCapture.setBounds(180, 2, 80, 25);
		doCapture.addActionListener(e -> {
			dataCollector.submitDataSample(presetSelect.getSelectedItem().toString(), cameraSpaceSamples);
			if (presetSelect.getSelectedIndex() < presetSelect.getItemCount() - 1) {
				presetSelect.setSelectedIndex(presetSelect.getSelectedIndex() + 1);
			}
		});
		frame.add(doCapture);
		
		doSave = new JButton("Save");
		doSave.setBounds(260, 2, 70, 25);
		doSave.addActionListener(e -> dataCollector.save(tfFileName.getText()));
		frame.add(doSave);
		
		doLoadAndCalibrate = new JButton("Load&Calibrate");
		doLoadAndCalibrate.setBounds(330, 2, 100, 25);
		doLoadAndCalibrate.addActionListener(e -> dataCollector.solveExtrinsicAndSave(tfFileName.getText()));
		frame.add(doLoadAndCalibrate);
		
		tfFileName = new JTextField("samples.json");
		tfFileName.setBounds(450, 2, 180, 25);
		frame.add(tfFileName);
		
		chkStartStopTracking = new JCheckBox("Tracking");
		chkStartStopTracking.setBounds(640, 2, 80, 25);
		frame.add(chkStartStopTracking);
		
		chkShutdownTrackers = new JCheckBox("ShutdownTrackers");
		chkShutdownTrackers.setBounds(720, 2, 120, 25);
		frame.add(chkShutdownTrackers);
		
		frame.setSize(1800, 800);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	@Override
	public void trackingUpdated(Collection<CameraSpaceFrame> rawData, Collection<WorldSpaceVertex> resolved, long timeMillis) {
		SwingUtilities.invokeLater(() -> {
			cameraSpaceSamples = rawData;
			
			StringBuilder sb = new StringBuilder();
			if (resolved != null) {
				sb.append("\n\n");
				for (WorldSpaceVertex k : resolved) {
					sb.append(String.format("%s - %.3f, %.3f, %.3f\n", k.getId(), k.getX(), k.getY(), k.getZ()));
				}
			}
			
			for (CameraSpaceFrame e : rawData) {
				sb.append("cam-").append(e.getCameraName()).append("\n")
						.append(e.getArucos().stream().map(
								(p) -> String.format("%d: (%.3f,%.3f)", p.getId(), p.getX(), p.getY())
						).collect(Collectors.joining(" "))).append("\n\n");
			}
			
			frame.setTitle("Camera calibration. Time: "+timeMillis);
			label.setText(sb.toString());
		});
	}
	
	public void show(String text) {
		SwingUtilities.invokeLater(() -> {
			label.setText(text);
		});
	}
	
	public boolean isTracking() {
		return chkStartStopTracking.isSelected();
	}
	
	public boolean isShutdownTrackers() {
		return chkShutdownTrackers.isSelected();
	}

}
