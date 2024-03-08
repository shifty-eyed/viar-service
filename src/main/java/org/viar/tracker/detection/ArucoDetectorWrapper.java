package org.viar.tracker.detection;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.objdetect.Objdetect;
import org.viar.core.ConvertUtil;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFeature;

import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArucoDetectorWrapper implements FeatureDetector {

    private ArucoDetector aruco;
    private DetectorParameters parameters;

    private final List<Mat> detectedCorners = new ArrayList<>(100);
    private final Mat detectedIds = new Mat();

    private double markerSize = 0.065;

    public ArucoDetectorWrapper() {
        var dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_100);
        aruco = new ArucoDetector(dictionary);
    }

    @Override
    public Collection<CameraSpaceFeature> detect(Mat frame, CameraSetup cameraSetup) {
        detectedCorners.clear();
        aruco.detectMarkers(frame, detectedCorners, detectedIds);

        List<CameraSpaceFeature> result = new ArrayList<>(detectedCorners.size());

        for (int i = 0; i < detectedCorners.size(); i++) {
            var data = new CameraSpaceFeature();
            data.setX(detectedCorners.get(i).get(0, 0)[0]);
            data.setY(detectedCorners.get(i).get(0, 0)[1]);
            data.setId((int)detectedIds.get(i, 0)[0]);
            data.setCameraName(cameraSetup.getName());
            data.setObjectName("Aruco");
            result.add(data);
        }
        return result;
    }

    public void drawMarkers(Mat frame, CameraSetup cameraSetup) {
        Objdetect.drawDetectedMarkers(frame, detectedCorners, detectedIds);

        final var half = markerSize / 2.0;
        MatOfPoint3f objectPoints = new MatOfPoint3f(
            new Point3(-half, half, 0f),
            new Point3(half, half, 0),
            new Point3(half, -half, 0),
            new Point3(-half, -half, 0)
        );

        for (var corners : detectedCorners) {
            Mat rvec = new Mat();
            Mat tvec = new Mat();
            MatOfPoint2f imagePoints = new MatOfPoint2f(corners.t());

            var intrinsics = cameraSetup.getIntrinsic();
            Calib3d.solvePnP(objectPoints, imagePoints, intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(),
                rvec, tvec);


            MatOfPoint3f featurePointOffset = new MatOfPoint3f(new Point3(0, 0, -0.06f));
            MatOfPoint2f outFeaturePoint = new MatOfPoint2f();

            Calib3d.projectPoints(featurePointOffset, rvec, tvec, intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(),
                outFeaturePoint);

            Calib3d.drawFrameAxes(frame, intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(), rvec, tvec,
                (float)markerSize * 1.5f, 2);

            Point featurePoint = new Point(outFeaturePoint.get(0, 0)[0], outFeaturePoint.get(0, 0)[1]);
            Imgproc.circle(frame, featurePoint, 5, new Scalar(0, 255, 255), 2);
        }

    }

}
