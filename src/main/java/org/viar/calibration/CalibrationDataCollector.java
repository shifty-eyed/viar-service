package org.viar.calibration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.vecmath.Point3d;

import org.apache.commons.io.IOUtils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.calibration.model.CalibrationData;
import org.viar.calibration.model.CalibrationSample;
import org.viar.calibration.model.CameraSamplesSet;
import org.viar.core.ConvertUtil;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class CalibrationDataCollector {

    private Gson gson;

    private static final String workingDir = "../calibration/";
    private static final double inch = 0.0254;

    private Map<String, List<CalibrationSample>> data;

    @Autowired
    private WorldCoordinatesPresets coordProvider;

    @Autowired
    private Map<String, CameraSetup> camerasConfig;

    @PostConstruct
    private void init() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        data = new TreeMap<>();
    }

    public void submitDataSample(String presetName, Collection<CameraSpaceFrame> sample) {
        sample.forEach(e -> {
            final Set<CalibrationSample> sampleSet = e.getArucos().stream().map(raw -> {
                Point3d worldSpace;
                try {
                    worldSpace = coordProvider.getWorldCoords(presetName, raw.getId());
                } catch (Exception e1) {
                    System.err.println(e1.getMessage());
                    return null;
                }
                return new CalibrationSample(raw.getX(), raw.getY(), worldSpace.x, worldSpace.y, worldSpace.z);
            }).filter(r -> r != null).collect(Collectors.toSet());

            List<CalibrationSample> existing = data.get(e.getCameraName());
            if (existing == null) {
                existing = new ArrayList<>();
                data.put(e.getCameraName(), existing);
            }
            existing.addAll(sampleSet);
        });
        System.out.println("Captured: " + presetName + ", cameras: " + sample.size());

    }

    public void save(String filename) {
        try (FileOutputStream out = new FileOutputStream(workingDir + filename)) {
            IOUtils.write(gson.toJson(
                    new CalibrationData(
                            data.entrySet().stream().map(e -> new CameraSamplesSet(e.getKey(), e.getValue())).toList()),
                    CalibrationData.class), out, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private CalibrationData load(String filename) {
        try (Reader in = new InputStreamReader(new FileInputStream(workingDir + filename))) {
            return gson.fromJson(in, CalibrationData.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void solveExtrinsic(Mat cameraMatrix, MatOfDouble distCoefficients, List<CalibrationSample> samples,
                                Mat outRvec, Mat outTvec) {
        Point3[] objectPointsData = new Point3[samples.size()];
        Point[] imagePointsData = new Point[samples.size()];

        final double unit = inch;
        int i = 0;

        for (CalibrationSample sample : samples) {
            objectPointsData[i] = new Point3(sample.getX() * unit, sample.getY() * unit, sample.getZ() * unit);
            imagePointsData[i++] = new Point(sample.getU(), sample.getV());
        }

        MatOfPoint3f objectPoints = new MatOfPoint3f(objectPointsData);
        MatOfPoint2f imagePoints = new MatOfPoint2f(imagePointsData);

        Calib3d.solvePnP(objectPoints, imagePoints, cameraMatrix, distCoefficients, outRvec, outTvec);
    }

    public void solveExtrinsicAndSave(String filename) {
        CalibrationData calibData = load(filename);
        StringBuilder sb = new StringBuilder();

        for (CameraSamplesSet samples : calibData.getCalibrationData()) {
            CameraSetup.Intrinsic intrinsic = camerasConfig.get(samples.getCameraName()).getIntrinsic();

            Mat rvec = new Mat();
            Mat tvec = new Mat();
            solveExtrinsic(intrinsic.getCameraMatrix(), intrinsic.getDistCoefficients(),
                    samples.getCalibrationSamples(), rvec, tvec);

            sb.append(String.format("\"%s\":\nrvec: %s\ntvec: %s\n\n", samples.getCameraName(),
                    ConvertUtil.stringOfMatLine(rvec), ConvertUtil.stringOfMatLine(tvec)));

        }
        try (FileOutputStream out = new FileOutputStream(workingDir + "extrinsic-" + filename)) {
            IOUtils.write(sb.toString(), out, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
