package org.viar.tracker.detection;

import org.opencv.aruco.Aruco;
import org.opencv.core.Mat;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Dictionary;
import org.viar.core.model.CameraSpaceFeature;

import java.util.Collection;

public class ArucoDetectorWrapper implements FeatureDetector {

    private Dictionary dictionary;
    private DetectorParameters parameters;

    public ArucoDetectorWrapper() {
        dictionary = ArucoDetector.;
        parameters = DetectorParameters.create();
    }

    @Override
    public Collection<CameraSpaceFeature> detect(Mat frame) {
        return null;
    }
}
