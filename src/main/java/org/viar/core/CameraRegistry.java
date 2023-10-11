package org.viar.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import org.apache.commons.io.IOUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.viar.core.model.CameraSetup;

import com.google.gson.Gson;

@Configuration
@SuppressWarnings("unchecked")
public class CameraRegistry {
	
	@Value("file:conf/cameras.json")
	private Resource camerasFile;
	
	@Value("file:conf/nodes.json")
	private Resource nodesFile;
	
	public static MatOfDouble parseMatOfDouble(String key, Map<String, Object> raw) {
		final List<Double> data = (List<Double>)raw.get(key);
		if (data == null || data.size() < 3) {
			throw new IllegalArgumentException("Wrong or missing: " + key);
		}
		MatOfDouble vec = new MatOfDouble();
    	vec.fromList(data);
		return vec;
	}
	
	public static Vector3d parseVector3d(String key, Map<String, Object> raw) {
		final List<Double> data = (List<Double>)raw.get(key);
		if (data == null || data.size() < 3) {
			throw new IllegalArgumentException("Wrong or missing: " + key);
		}
		return new Vector3d(data.get(0), data.get(1), data.get(2));
	}
	
	public static Mat parseCameraMartix(String key, Map<String, Object> raw) {
		final List<Double> data = (List<Double>)raw.get(key);
		if (data == null || data.size() < 3) {
			throw new IllegalArgumentException("Wrong or missing: " + key);
		}
		double f = data.get(0);
		double cx = data.get(1);
		double cy = data.get(2);
		Mat result = Mat.zeros(3, 3, CvType.CV_64F);
		result.put(0, 0, f);
		result.put(1, 1, f);
		result.put(0, 2, cx);
		result.put(1, 2, cy);
		result.put(2, 2, 1);
		return result;
	}
	
	@Bean
	public Map<String, CameraSetup> camerasConfig() throws IOException {
		Map<String, CameraSetup> result = new HashMap<>();
		
		try(InputStream in = camerasFile.getInputStream()) {
            final String data = IOUtils.toString(in, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Map<String, Map<String, Object>> camerasRaw = gson.fromJson(data, Map.class);
            for (Map.Entry<String, Map<String, Object>> entry : camerasRaw.entrySet()) {
            	Map<String, Object> raw = entry.getValue();
            	
            	int deviceNumber = ((Double)raw.get("deviceNumber")).intValue();
            	MatOfDouble rvec = parseMatOfDouble("rvec", raw);
            	MatOfDouble tvec = parseMatOfDouble("tvec", raw);
            	MatOfDouble distCoefficients = parseMatOfDouble("distCoefficients", raw);
            	
            	Vector3d eye = parseVector3d("eye", raw);
            	Vector3d center = parseVector3d("center", raw);
            	Vector3d up = parseVector3d("up", raw);
            	
            	Mat cameraMatrix = parseCameraMartix("projection", raw);
            	result.put(entry.getKey(), new CameraSetup(entry.getKey(), deviceNumber, eye, center, up, cameraMatrix, distCoefficients, rvec, tvec));
            }
        }
		return result;
	}

}
