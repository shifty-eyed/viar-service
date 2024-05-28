package org.viar.tracker.tracking;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.tracking.TrackerKCF;
import org.opencv.video.Tracker;
import org.viar.core.model.CameraSpaceFeature;

import java.util.LinkedList;
import java.util.List;

/*
TODO: keep last couple of detected just one tracked, and predict should merge them all,
 if last was tracking, don't need to smooth because tracked result always smooth
 if last was detected, it may be too much off
   ? maybe dont even need to store the stack, just last tracked and detected and the time variable to use it as alpha for interpolation
 */
public class FeatureTrack {

	private static final int AGE_LIMIT = 10;

	private final CameraSpaceFeature initialRef;
	private int age;
	private Rect trackedRectAfterDetection;
	private Rect trackedRectBeforeDetection;
	private Tracker tracker;

	public FeatureTrack(CameraSpaceFeature initialReference) {
		this.initialRef = initialReference;
	}

	public CameraSpaceFeature submitDetected(Mat frame, Rect detectedRect) {
		tracker = TrackerKCF.create();
		tracker.init(frame, detectedRect);
		age = 0;
		trackedRectBeforeDetection = trackedRectAfterDetection;
		trackedRectAfterDetection = detectedRect;
		return predict();
	}

	public CameraSpaceFeature updateFrame(Mat frame) {
		Rect trackedRect = new Rect();
		try {
			if (tracker.update(frame, trackedRect)) {
				trackedRectAfterDetection = trackedRect;
			}
		} catch (Exception e) {
			System.out.println("Error tracking " + e.getMessage());
		}
		age++;
		return predict();
	}

	private static double interpolate(double a, double b, double alpha) {
		return (1.0 - alpha) * a + alpha * b;
	}

	private CameraSpaceFeature predict() {
		CameraSpaceFeature result = new CameraSpaceFeature(initialRef);
		var alpha = Math.min(1.0, (double) age / AGE_LIMIT);

		if (trackedRectBeforeDetection == null) {
			trackedRectBeforeDetection = trackedRectAfterDetection;
		}

		result.setWidth(interpolate(trackedRectBeforeDetection.width, trackedRectAfterDetection.width, alpha));
		result.setHeight(interpolate(trackedRectBeforeDetection.height, trackedRectAfterDetection.height, alpha));
		result.setX(interpolate(trackedRectBeforeDetection.x, trackedRectAfterDetection.x, alpha) + result.getWidth() / 2.0);
		result.setY(interpolate(trackedRectBeforeDetection.y, trackedRectAfterDetection.y, alpha) + result.getHeight() / 2.0);
		return result;
	}

}
