package org.viar.tracker.tracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.tracking.TrackerKCF;
import org.opencv.tracking.TrackerKCF_Params;
import org.viar.core.model.CameraSpaceFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FeatureTracker {

	@AllArgsConstructor
	private static class FeatureTrack {
		CameraSpaceFeature feature;
		TrackerKCF tracker;
	}

	private Map<String, FeatureTrack> trackers = new HashMap<>();
	private final TrackerKCF_Params params;


	public FeatureTracker() {
		params = new TrackerKCF_Params();
	}

	public void updateFeatures(Mat frame, Collection<CameraSpaceFeature> newFeatures) {
		for (var feature : newFeatures) {
			var key = feature.getUniqueName();
			var bundle = trackers.get(key);
			if (bundle == null) {
				var tracker = TrackerKCF.create(params);
				bundle = new FeatureTrack(feature, tracker);
				trackers.put(key, bundle);
			}
			bundle.tracker.init(frame, feature.getRoI());
			bundle.feature = feature;

		};
	}

	public Collection<CameraSpaceFeature> trackFeatures(Mat frame) {
		var result = new ArrayList<CameraSpaceFeature>(trackers.size());
		for (var bundle : trackers.values()) {
			var roI = bundle.feature.getRoI();
			if (bundle.tracker.update(frame, roI)) {
				bundle.feature.setX(roI.x + roI.width / 2);
				bundle.feature.setY(roI.y + roI.height / 2);
				bundle.feature.setWidth(roI.width);
				bundle.feature.setHeight(roI.height);
				result.add(bundle.feature);
			}
		}
		return result;
	}


}
