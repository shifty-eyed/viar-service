package org.viar.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import javax.vecmath.Point3d;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.calibration.CalibrationDataCollector;
import org.viar.calibration.WorldCoordinatesPresets;
import org.viar.core.TrackingListener;
import org.viar.core.model.MarkerNode;
import org.viar.core.model.MarkerRawPosition;

@Component
public class Monitor implements TrackingListener {

	private JFrame frame;
	private JTextArea label;
	private JComboBox<String> presetSelect;
	private JButton doCapture;
	private JButton doSave;
	private JButton doLoadAndCalibrate;
	private JCheckBox doStartStopTracking;
	private JTextField tfFileName;
	
	@Autowired
	private WorldCoordinatesPresets coordProvider;
	
	@Autowired
	private CalibrationDataCollector dataCollector;
	
	private Map<String, Collection<MarkerRawPosition>> mostRecentData;

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
		doCapture.setBounds(170, 2, 100, 25);
		doCapture.addActionListener(e -> {
			dataCollector.submitDataSample(presetSelect.getSelectedItem().toString(), mostRecentData);
			if (presetSelect.getSelectedIndex() < presetSelect.getItemCount() - 1) {
				presetSelect.setSelectedIndex(presetSelect.getSelectedIndex() + 1);
			}
		});
		frame.add(doCapture);
		
		doSave = new JButton("Save");
		doSave.setBounds(300, 2, 100, 25);
		doSave.addActionListener(e -> dataCollector.save(tfFileName.getText()));
		frame.add(doSave);
		
		doLoadAndCalibrate = new JButton("Load&Calibrate");
		doLoadAndCalibrate.setBounds(430, 2, 120, 25);
		doLoadAndCalibrate.addActionListener(e -> dataCollector.solveExtrinsicAndSave(tfFileName.getText()));
		frame.add(doLoadAndCalibrate);
		
		doStartStopTracking = new JCheckBox("Tracking");
		doStartStopTracking.setBounds(550, 2, 120, 25);
		frame.add(doStartStopTracking);
		
		tfFileName = new JTextField("samples.json");
		tfFileName.setBounds(600, 2, 200, 25);
		frame.add(tfFileName);
		
		frame.setSize(1800, 800);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	@Override
	public void trackingUpdated(Map<String, Collection<MarkerRawPosition>> rawData, Map<MarkerNode, Point3d> resolved, long timeMillis) {
		SwingUtilities.invokeLater(() -> {
			mostRecentData = rawData;
			
			StringBuilder sb = new StringBuilder();
			if (resolved != null) {
				sb.append("\n\n");
				List<MarkerNode> nodeKeys = new ArrayList<>(resolved.keySet());
				Collections.sort(nodeKeys, (a, b) -> a.getId().compareTo(b.getId()));
				for (MarkerNode k : nodeKeys) {
					Point3d p = resolved.get(k);
					sb.append(String.format("%s - %.3f, %.3f, %.3f\n", k.getId(), p.x, p.y, p.z));
				}
			}
			
			for (Map.Entry<String, Collection<MarkerRawPosition>> e : rawData.entrySet()) {
				sb.append("cam-").append(e.getKey()).append("\n")
						.append(e.getValue().stream().map(
								(p) -> String.format("%d: (%.3f,%.3f)", p.getMarkerId(), p.getPosition().x, p.getPosition().y)
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
		return doStartStopTracking.isSelected();
	}

}
