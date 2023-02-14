package org.viar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.viar.core.model.MarkerRawPosition;
import org.viar.ui.Monitor;

@Component
public class CameraProcessor {

	private final int numCameras = 2;
	
	private final int frameWidthHalf = 1920 / 2;  
	private final int frameHeightHalf = 1080 / 2;  
	private final double frameScale = frameWidthHalf;  
	
	@Autowired
	private Monitor monitor;
	
	@Autowired
	private ObjectPositionResolver objectPositionResolver;

	private ExecutorService pool;
	private ExecutorService mainLoopRunner;

	@PostConstruct
	private void init() {
		mainLoopRunner = Executors.newSingleThreadExecutor();
		pool = Executors.newFixedThreadPool(numCameras);
		init(numCameras, true);
		mainLoopRunner.execute(mainLoop);
	}

	private Runnable mainLoop = new Runnable() {
		@Override
		public void run() {
			Map<Integer, Collection<MarkerRawPosition>> data = Collections.emptyMap();
			for (;;) {
				long time = System.currentTimeMillis();
				try {
					data = processFrames();
				} catch (Exception e) {
					e.printStackTrace();
				}
				time = System.currentTimeMillis() - time;

				if (data != null) {
					/*StringBuilder sb = new StringBuilder();
					for (Map.Entry<Integer, Collection<MarkerRawPosition>> e : data.entrySet()) {
						sb.append("cam-").append(e.getKey()).append("\n")
								.append(e.getValue().stream().map(
										(p) -> String.format("%d: (%.3f,%.3f)", p.getMarkerId(), p.getPosition().x, p.getPosition().y)
								).collect(Collectors.joining(" "))).append("\n\n");
					}
					monitor.onChange(sb.toString(), time);*/
					monitor.onChange(objectPositionResolver.resolve(data).toString(), time);
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

	public Map<Integer, Collection<MarkerRawPosition>> processFrames() throws InterruptedException, ExecutionException {

		List<Future<String[]>> tasks = new ArrayList<>(numCameras);
		for (int i = 0; i < numCameras; i++) {
			final int camId = i;
			tasks.add(pool.submit(() -> {
				return processFrame(camId);
			}));
		}

		Map<Integer, Collection<MarkerRawPosition>> result = new LinkedHashMap<>(numCameras);
		for (int i = 0; i < numCameras; i++) {
			String[] data = tasks.get(i).get();
			if (data != null && data.length > 0) {
				result.put(i, parseAndConvert(data));
			}
		}
		return result;
	}

	private native void init(int cameraCount, boolean serialCameraSelection);

	private native String[] processFrame(int cameraId);
	
}
