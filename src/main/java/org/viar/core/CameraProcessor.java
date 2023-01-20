package org.viar.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.ui.Monitor;

@Component
public class CameraProcessor {

	private final int numCameras = 4;
	
	@Autowired
	private Monitor monitor;

	private ExecutorService pool;
	private ExecutorService mainLoopRunner;

	@PostConstruct
	private void init() {
		mainLoopRunner = Executors.newSingleThreadExecutor();
		pool = Executors.newFixedThreadPool(numCameras);
		init(numCameras);
		mainLoopRunner.execute(mainLoop);
	}

	private Runnable mainLoop = new Runnable() {
		@Override
		public void run() {
			Map<Integer, Map<Integer, Point>> data = Collections.emptyMap();
			for (;;) {
				long time = System.currentTimeMillis();
				try {
					data = processFrames();
				} catch (Exception e) {
					e.printStackTrace();
				}
				time = System.currentTimeMillis() - time;

				if (data != null) {
					StringBuilder sb = new StringBuilder();
					for (Map.Entry<Integer, Map<Integer, Point>> e : data.entrySet()) {
						sb.append("cam-").append(e.getKey()).append("\n")
								.append(e.getValue().entrySet().stream().map((p) -> {
									return p.getKey() + ":(" + p.getValue().x + "," + p.getValue().y + ")";
								}).collect(Collectors.joining(" "))).append("\n\n");
					}
					monitor.onChange(sb.toString(), time);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}

		}
	};

	private Map<Integer, Point> parse(String[] data) {
		return Arrays.stream(data).map((line) -> {
			return Arrays.stream(line.split(" ")).map(Integer::parseInt).toArray(Integer[]::new);
		}).collect(Collectors.toMap((parts) -> parts[0], (parts) -> new Point(parts[1], parts[2])));
	}

	public Map<Integer, Map<Integer, Point>> processFrames() throws InterruptedException, ExecutionException {

		List<Future<String[]>> tasks = new ArrayList<>(numCameras);
		for (int i = 0; i < numCameras; i++) {
			final int camId = i;
			tasks.add(pool.submit(() -> {
				return processFrame(camId);
			}));
		}

		Map<Integer, Map<Integer, Point>> result = new LinkedHashMap<>(numCameras);
		for (int i = 0; i < numCameras; i++) {
			String[] data = tasks.get(i).get();
			if (data != null && data.length > 0) {
				result.put(i, parse(data));
			}
		}
		return result;
	}

	private native void init(int cameraCount);

	private native String[] processFrame(int cameraId);

	// private native String calibrateFrame(int cameraId);

}
