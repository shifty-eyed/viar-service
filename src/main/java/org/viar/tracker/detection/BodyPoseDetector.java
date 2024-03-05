package org.viar.tracker.detection;

import org.opencv.core.Mat;
import org.viar.core.model.CameraSpaceFeature;

import java.util.Collection;

public class BodyPoseDetector implements FeatureDetector {

    @Override
    public Collection<CameraSpaceFeature> detect(Mat frame) {
        return null;
    }
}
