package org.viar.tracker.detection;

import org.opencv.core.Mat;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFeature;

import java.util.Collection;

public interface FeatureDetector {

     Collection<CameraSpaceFeature> detect(Mat frame, CameraSetup cameraSetup);

}
