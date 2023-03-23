package org.viar.calibration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.vecmath.Point3d;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.calibration.model.CalibrationData;
import org.viar.calibration.model.CalibrationSample;
import org.viar.calibration.model.CameraSamplesSet;
import org.viar.core.model.MarkerRawPosition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class CalibrationDataCollector {

	private Gson gson;

	private static final String workingDir = "d:/ws/calib/";
	
	private Map<String, Set<CalibrationSample>> data;

	@Autowired
	private WorldCoordinatesPresets coordProvider;

	@PostConstruct
	private void init() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		data = new TreeMap<>();
	}

	public void submitDataSample(String presetName, Map<String, Collection<MarkerRawPosition>> sample) {
		 sample.entrySet().forEach(e -> {
			 final String cameraId = e.getKey();
			 final Set<CalibrationSample> sampleSet = e.getValue().stream().map(raw -> {
				 Point3d worldSpace =  coordProvider.getWorldCoords(presetName, raw.getMarkerId());
				 return new CalibrationSample(raw.getPosition().x, raw.getPosition().y,
						 worldSpace.x, worldSpace.y, worldSpace.z);
			 }).collect(Collectors.toSet());
			 
			 Set<CalibrationSample> existing = data.get(cameraId);
			 if (existing == null) {
				 existing = new HashSet<>();
				 data.put(cameraId, existing);
			 }
			 existing.addAll(sampleSet);
		 });

	}

	public void save(String filename) {
		try (FileOutputStream out = new FileOutputStream(workingDir + filename)) {
			IOUtils.write(gson.toJson(
					new CalibrationData(
							data.entrySet().stream().map(e -> new CameraSamplesSet(e.getKey(), e.getValue())).toList()),
					CalibrationData.class), out, "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void load(String filename) {
		try (Reader in = new InputStreamReader(new FileInputStream(workingDir + filename))) {
			CalibrationData d = gson.fromJson(in, CalibrationData.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
