package org.viar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.MarkerRawPosition;
import org.viar.ui.Monitor;

@Component
public class CameraProcessor {

	private final int numCameras = 4;
	
	private final int frameWidthHalf = 1920 / 2;  
	private final int frameHeightHalf = 1080 / 2;  
	private final double frameScale = frameWidthHalf;  
	
	@Autowired
	private Monitor monitor;
	
	@Autowired
	private ObjectPositionResolver objectPositionResolver;
	
	@Autowired
	private Map<String, CameraSetup> camerasConfig;

	private ExecutorService pool;
	private ExecutorService mainLoopRunner;
	private Map<Integer, String> deviceNumberToCameraName = new HashMap<>();

	@PostConstruct
	private void init() {
		camerasConfig.values().forEach(setup -> deviceNumberToCameraName.put(setup.getDeviceNumber(), setup.getId()));
		mainLoopRunner = Executors.newSingleThreadExecutor();
		pool = Executors.newFixedThreadPool(numCameras);
		init(numCameras, true);
		mainLoopRunner.execute(mainLoop);
		
		
	}

	private Runnable mainLoop = new Runnable() {
		@Override
		public void run() {
			Map<String, Collection<MarkerRawPosition>> data = Collections.emptyMap();
			for (;;) {
				long time = System.currentTimeMillis();
				try {
					//data = processFramesMock();
					data = processFrames();
					/*if (data.size() == 2) {
						System.out.println(data);
					}*/
				} catch (Exception e) {
					e.printStackTrace();
				}
				time = System.currentTimeMillis() - time;

				if (data != null) {
					//sb.append("\n").append(objectPositionResolver.resolve(data).toString());
					monitor.onChange(data, time);
				}
				Thread.yield();
			}

		}
	};

	private Collection<MarkerRawPosition> parseAndConvert(String[] data) {
		return Arrays.stream(data).map((line) -> {
			return Arrays.stream(line.split(" ")).map(Integer::parseInt).toArray(Integer[]::new);
		}).map((parts) -> {
			return new MarkerRawPosition(parts[0], new Point(parts[1], parts[2]));
		}).toList();
	}

	public Map<String, Collection<MarkerRawPosition>> processFrames() throws InterruptedException, ExecutionException {

		List<Future<String[]>> tasks = new ArrayList<>(numCameras);
		for (int i = 0; i < numCameras; i++) {
			final int camId = i;
			tasks.add(pool.submit(() -> {
				return processFrame(camId);
			}));
		}

		Map<String, Collection<MarkerRawPosition>> result = new LinkedHashMap<>(numCameras);
		for (int i = 0; i < numCameras; i++) {
			String[] data = tasks.get(i).get();
			if (data != null && data.length > 0) {
				result.put(deviceNumberToCameraName.get(i), parseAndConvert(data));
			}
		}
		return result;
	}
	
	public Map<Integer, Collection<MarkerRawPosition>> processFramesMock() throws InterruptedException, ExecutionException {
		
		
		Map<Integer, Collection<MarkerRawPosition>> result = new LinkedHashMap<>(numCameras);
		result.put(0, parseAndConvert(new String[]{"0 462 367"}));
		result.put(1, parseAndConvert(new String[]{"0 1383 531"}));
		return result;
	}

	private native void init(int cameraCount, boolean serialCameraSelection);

	private native String[] processFrame(int cameraId);
	
}
