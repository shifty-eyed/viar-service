package org.viar.tracker.detection;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Objdetect;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArucoDetectorWrapper implements FeatureDetector {

    private ArucoDetector aruco;
    private DetectorParameters parameters;

    public ArucoDetectorWrapper() {
        var dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_100);
        aruco = new ArucoDetector(dictionary);
    }

    @Override
    public Collection<CameraSpaceFeature> detect(Mat frame, CameraSetup cameraSetup) {
        List<Mat> corners = new ArrayList<>(100);
        Mat ids = new Mat();
        aruco.detectMarkers(frame, corners, ids);

        List<CameraSpaceFeature> result = new ArrayList<>(corners.size());
        for (int i = 0; i < corners.size(); i++) {
            var data = new CameraSpaceFeature();
            data.setX(corners.get(i).get(0, 0)[0]);
            data.setY(corners.get(i).get(0, 0)[1]);
            data.setId((int)ids.get(i, 0)[0]);
            data.setCameraName(cameraSetup.getName());
            data.setObjectName("Aruco");
            result.add(data);
        }
        return result;
    }

    private Point3 resolveNormalVectorByCameraSetupAndArucoCorners(CameraSetup cameraSetup, List<Point> corners) {
        
    }
}
