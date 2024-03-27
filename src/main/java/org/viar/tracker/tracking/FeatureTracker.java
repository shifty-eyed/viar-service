package org.viar.tracker.tracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.tracking.TrackerCSRT;
import org.opencv.tracking.TrackerKCF;
import org.opencv.tracking.TrackerKCF_Params;
import org.opencv.video.*;
import org.viar.core.model.CameraSpaceFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FeatureTracker {

	@AllArgsConstructor
	private static class FeatureTrack {
		CameraSpaceFeature feature;
		Tracker tracker;
	}

	private Map<String, FeatureTrack> trackers = new HashMap<>();
	private final TrackerKCF_Params params;


	public FeatureTracker() {
		params = new TrackerKCF_Params();
	}

	public synchronized void updateFeatures(Mat frame, Collection<CameraSpaceFeature> newFeatures) {
		//If need to track a nose only
		//newFeatures = newFeatures.stream().filter(f -> f.getId() == 0).toList();
		for (var feature : newFeatures) {
			var key = feature.getUniqueName();
			var bundle = trackers.get(key);
			if (bundle == null) {
				bundle = new FeatureTrack(feature, null);
				trackers.put(key, bundle);

			}
			bundle.tracker = TrackerKCF.create();
			bundle.tracker.init(frame, feature.getRoI());
			bundle.feature = feature;

		};
	}

	public synchronized Collection<CameraSpaceFeature> trackFeatures(Mat frame) {
		final var result = new ArrayList<CameraSpaceFeature>(trackers.size());
		trackers.values().parallelStream().forEach(bundle -> {
			var roI = bundle.feature.getRoI();
			var success = false;
			try {
				success = bundle.tracker.update(frame, roI);
			} catch (Exception e) {
				System.out.println("Error tracking " + e.getMessage());
			}
			if (success) {
				bundle.feature.setX(roI.x + roI.width / 2);
				bundle.feature.setY(roI.y + roI.height / 2);
				bundle.feature.setWidth(roI.width);
				bundle.feature.setHeight(roI.height);
				result.add(bundle.feature);
			}
		});
		return result;
	}


}
