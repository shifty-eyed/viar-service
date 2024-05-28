package org.viar.tracker.tracking;

import org.opencv.core.Mat;
import org.viar.core.model.CameraSpaceFeature;

import java.util.*;

public class TrackingRegistry {

	private Map<String, FeatureTrack> trackers = new HashMap<>();


	public synchronized void submitDetected(Mat frame, Collection<CameraSpaceFeature> newFeatures) {
		//If need to track a nose only
		//newFeatures = newFeatures.stream().filter(f -> f.getId() == 0).toList();
		for (var feature : newFeatures) {
			var key = feature.getUniqueName();
			var bundle = trackers.get(key);
			if (bundle == null) {
				bundle = new FeatureTrack(feature);
				trackers.put(key, bundle);

			}
			bundle.submitDetected(frame, feature.getRoI());
		};
	}

	public synchronized Collection<CameraSpaceFeature> trackFeatures(Mat frame) {
		return trackers.values().parallelStream().map(bundle -> bundle.updateFrame(frame)).toList();
	}

}
