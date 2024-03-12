package org.viar.tracker.detection;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Objdetect;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFeature;
import org.viar.tracker.model.MakerFeaturePointOffset;

import java.util.*;

public class ArucoDetectorWrapper implements FeatureDetector {

    private ArucoDetector aruco;

    private final List<Mat> detectedCorners = new ArrayList<>(100);
    private final Mat detectedIds = new Mat();

    private final double markerSize;

    private final MatOfPoint3f markerObjectWorldSpacePoints;

    private final Map<Integer, MakerFeaturePointOffset> markerFeaturePointOffsets;

    private final Deque<Point> featureCerterHistory = new LinkedList<>();
    private static final int FEATURE_CENTER_HISTORY_SIZE = 6;

    public ArucoDetectorWrapper(double markerSize, Map<Integer, MakerFeaturePointOffset> markerFeaturePointOffsets) {
        this.markerFeaturePointOffsets = markerFeaturePointOffsets;
        this.markerSize = markerSize;

        var dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_100);
        aruco = new ArucoDetector(dictionary);

        final var half = markerSize / 2.0;
        markerObjectWorldSpacePoints = new MatOfPoint3f(
            new Point3(-half, half, 0f),
            new Point3(half, half, 0),
            new Point3(half, -half, 0),
            new Point3(-half, -half, 0)
        );
    }

    @Override
    public Collection<CameraSpaceFeature> detect(Mat frame, CameraSetup cameraSetup) {
        detectedCorners.clear();
        aruco.detectMarkers(frame, detectedCorners, detectedIds);
        return solveFeatureOffsetPositionByDetectedCornersAndIntrinsics(detectedCorners, detectedIds, cameraSetup);
    }

    private long lastTime = 0;

    private List<CameraSpaceFeature> solveFeatureOffsetPositionByDetectedCornersAndIntrinsics(List<Mat> detectedCorners, Mat detectedIds, CameraSetup cameraSetup) {

        List<CameraSpaceFeature> result = new ArrayList<>(detectedCorners.size());

        for (int i=0; i<detectedCorners.size(); i++){
            var singleMarker4Corners = detectedCorners.get(i);
            var singleMarkerId = (int)detectedIds.get(i, 0)[0];

            final Mat rvec = new Mat();
            final Mat tvec = new Mat();
            final MatOfPoint2f imagePoints = new MatOfPoint2f(singleMarker4Corners.t());

            var intrinsics = cameraSetup.getIntrinsic();
            Calib3d.solvePnP(markerObjectWorldSpacePoints, imagePoints,
                intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(),
                rvec, tvec);

            var featurePointConfig = markerFeaturePointOffsets.getOrDefault(singleMarkerId, new MakerFeaturePointOffset());
            MatOfPoint2f outFeaturePoint = new MatOfPoint2f();

            Calib3d.projectPoints(new MatOfPoint3f(featurePointConfig.getPoint3()), rvec, tvec,
                intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(),
                outFeaturePoint);

            var featureCenterProjected = new Point(outFeaturePoint.get(0, 0)[0], outFeaturePoint.get(0, 0)[1]);

            var featureCenter = smoothJitter(featureCenterProjected, imagePoints);

            var feature = new CameraSpaceFeature();
            feature.setX(featureCenter.x);
            feature.setY(featureCenter.y);
            feature.setId(featurePointConfig.getMarkerId());
            feature.setCameraName(cameraSetup.getName());
            feature.setObjectName("Aruco");
            result.add(feature);
        }
        return result;
    }

    private Point smoothJitter(Point featureCenterProjected, MatOfPoint2f imagePoints) {
        // snap to mean if close enough to center
        var featureCenterMean = new Point((imagePoints.get(0, 0)[0] + imagePoints.get(2, 0)[0]) / 2,
            (imagePoints.get(0, 0)[1] + imagePoints.get(2, 0)[1]) / 2);

        if (Math.abs(featureCenterProjected.x - featureCenterMean.x) < 5) {
            featureCenterProjected.x = featureCenterMean.x;
        }
        if (Math.abs(featureCenterProjected.y - featureCenterMean.y) < 5) {
            featureCenterProjected.y = featureCenterMean.y;
        }

        // smooth jitter
        featureCerterHistory.addLast(featureCenterProjected);
        while (featureCerterHistory.size() > FEATURE_CENTER_HISTORY_SIZE) {
            featureCerterHistory.removeFirst();
        }
        var historycalMean = featureCerterHistory.stream().reduce(new Point(0, 0), (a, b) -> new Point(a.x + b.x, a.y + b.y));
        historycalMean.x /= featureCerterHistory.size();
        historycalMean.y /= featureCerterHistory.size();

        return historycalMean;
    }

    public void drawMarkers(Mat frame, CameraSetup cameraSetup) {
        Objdetect.drawDetectedMarkers(frame, detectedCorners, detectedIds);

        for (var corners : detectedCorners) {
            Mat rvec = new Mat();
            Mat tvec = new Mat();
            MatOfPoint2f imagePoints = new MatOfPoint2f(corners.t());

            var intrinsics = cameraSetup.getIntrinsic();
            Calib3d.solvePnP(markerObjectWorldSpacePoints, imagePoints, intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(),
                rvec, tvec);

            Calib3d.drawFrameAxes(frame, intrinsics.getCameraMatrix(), intrinsics.getDistCoefficients(), rvec, tvec,
                (float)markerSize * 1.5f, 2);

        }

    }

}
