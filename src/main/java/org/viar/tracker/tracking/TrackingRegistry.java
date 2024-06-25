package org.viar.tracker.tracking;

import org.opencv.core.Mat;
import org.viar.core.model.CameraSpaceFeature;

import java.util.*;

public class TrackingRegistry {

	private final int ageLimit;

	private Map<String, FeatureTrack> trackers = new HashMap<>();

	public TrackingRegistry(int ageLimit) {
		this.ageLimit = ageLimit;
	}

	public synchronized Collection<CameraSpaceFeature> submitDetected(Mat frame, Collection<CameraSpaceFeature> newFeatures) {
		return newFeatures.stream().map(feature -> {
			var key = feature.getUniqueName();
			var bundle = trackers.get(key);
			if (bundle == null) {
				bundle = new FeatureTrack(feature, ageLimit);
				trackers.put(key, bundle);

			}
			return bundle.submitDetected(frame, feature.getRoI());
		}).toList();
	}

	public synchronized Collection<CameraSpaceFeature> trackFeatures(Mat frame) {
		return trackers.values().parallelStream().map(bundle -> bundle.updateFrame(frame)).toList();
	}

}
