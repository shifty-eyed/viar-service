package org.viar.tracker;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.viar.core.StopwatchStats;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFeature;
import org.viar.tracker.detection.ArucoDetectorWrapper;
import org.viar.tracker.detection.BodyPoseDetector;
import org.viar.tracker.model.MakerFeaturePointOffset;
import org.viar.tracker.tracking.TrackingRegistry;
import org.viar.tracker.ui.DetectionAndTrackingLab;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Profile("lab")
@Component
public class LabMonitor implements Runnable {

    private final Dimension frameSize = new Dimension(1920, 1080);
    private static final int REDETECT_EVERY_FRAMES = 5;

    @Setter @Getter
    private boolean running = true;

    private JFrame window;
    private DetectionAndTrackingLab labUI;

    private VideoCapture capture;
    private Mat frameSrc;
    private Mat frameMarkup;

    private ArucoDetectorWrapper arucoDetector;
    private BodyPoseDetector bodyPoseDetector;
    private TrackingRegistry featureTracker;

    private VideoWriter videoWriter;
    private ExecutorService pool;
    private int detectionFrameCount = 0;

    @Autowired
    private StopwatchStats stats;

    @Autowired
    private Map<String, CameraSetup> camerasConfig;

    @Autowired
    private Map<Integer, MakerFeaturePointOffset> markerFeaturePointOffsets;

    private static final String CAP_FILE = "media/body-movements1.avi";

    @PostConstruct
    private void init() throws InterruptedException {
        arucoDetector = new ArucoDetectorWrapper(0.065, markerFeaturePointOffsets);
        bodyPoseDetector = new BodyPoseDetector();
        featureTracker = new TrackingRegistry(REDETECT_EVERY_FRAMES);

        if (CAP_FILE != null) {
            capture = new VideoCapture(CAP_FILE);
        } else {
            capture = new VideoCapture(0, Videoio.CAP_V4L2, new MatOfInt(
                Videoio.CAP_PROP_FOURCC, VideoWriter.fourcc('M', 'J', 'P', 'G'),
                Videoio.CAP_PROP_FRAME_WIDTH, frameSize.width,
                Videoio.CAP_PROP_FRAME_HEIGHT, frameSize.height
            ));
        }


        frameSrc = new Mat();
        frameMarkup = new Mat();

        initUI();
        checkCaptureErrors();

        //DEBUG
        //frameSrc = Imgcodecs.imread("models/pose1-black.jpg");
        //Imgproc.threshold(frameSrc, frameSrc, 190, 255, Imgproc.THRESH_BINARY);
    }

    @Override
    public void run() {
        var frameCount = 0;
        var startTime = System.currentTimeMillis();
        while (running) {
            synchronized (frameSrc) {
                var gotFrame = capture.read(frameSrc);
                if (!gotFrame && CAP_FILE != null) {
                    capture.set(Videoio.CAP_PROP_POS_FRAMES, 0);
                    continue;
                }
            }
            frameMarkup = frameSrc.clone();
            if (videoWriter != null) {
                videoWriter.write(frameSrc);
            } else {
                processFrame(frameSrc, frameMarkup);
            }

            var javaImage = HighGui.toBufferedImage(frameMarkup);
            labUI.imagePanel.getGraphics().drawImage(javaImage, 0, 0, null);

            frameCount++;
            if (System.currentTimeMillis() - startTime > 1000) {
                labUI.labelFPS.setText(String.format("%d FPS", frameCount));
                //labUI.textStats.setText(stats.getStats());
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
        System.out.println("Shutting down");
        capture.release();
        System.out.println("Capture released");
    }

    private void processFrame(Mat frameSrc, Mat frameMarkup) {
        var features = detectionFrameCount == 0 ? refreshDetection() : featureTracker.trackFeatures(frameSrc);
        detectionFrameCount++;
        if (detectionFrameCount > REDETECT_EVERY_FRAMES) {
            detectionFrameCount = 0;
        }
        drawFeatures(frameMarkup, features);
    }

    private Collection<CameraSpaceFeature> refreshDetection() {
        stats.start("body.detect");
        var detectedFeatures = bodyPoseDetector.detect(frameSrc, camerasConfig.get("2"));
        stats.stop("body.detect");
        //stats.start("aruco.detect");
        //var features = arucoDetector.detect(frameSrc, camerasConfig.get("2"));
        //stats.stop("aruco.detect");


        stats.start("updateFeatures");
        var balancedFeatures = featureTracker.submitDetected(frameSrc, detectedFeatures);
        stats.stop("updateFeatures");
        return balancedFeatures;
    }

    private void drawFeatures(Mat frame, Collection<CameraSpaceFeature> features) {
        //arucoDetector.drawMarkers(frame, camerasConfig.get("2"));
        //bodyPoseDetector.drawHeatMaps(frame, 18);

        final var color = new Scalar(0, 200, 0);
        final var yellow = new Scalar(0, 200, 200);
        for (var feature : features) {
            var position = new Point(feature.getX(), feature.getY());
            var roi = feature.getRoI();
            /*if (roi != null) {
                Imgproc.rectangle(frame, feature.getRoI(), color, 2);
            } else {
                Imgproc.circle(frame, position, 3, color, 2);
            }*/
            Imgproc.circle(frame, position, 10, color, -1);

            //Imgproc.putText(frame, String.valueOf(feature.getId()), position, Imgproc.FONT_HERSHEY_SIMPLEX, 1.5, yellow, 2);
        }
    }

    private void checkCaptureErrors() throws InterruptedException {
        //check if the camera is opened and able to capture frames
        if (!capture.isOpened()) {
            System.out.println("Error - cannot open camera");
            JOptionPane.showMessageDialog(window, "Cannot open camera", "Error", JOptionPane.ERROR_MESSAGE);
            shutdown();
            System.exit(1);
        } else {
            capture.read(frameSrc);
            if (frameSrc.width() == frameSize.width && frameSrc.height() == frameSize.height) {
                pool = Executors.newSingleThreadExecutor();
                pool.execute(this);
            } else {
                System.out.println("Error - cannot capture requested frame size");
                JOptionPane.showMessageDialog(window, "Cannot capture requested frame size", "Error", JOptionPane.ERROR_MESSAGE);
                shutdown();
                System.exit(1);
            }
        }
    }

    private void initUI() {
        window = new JFrame("Lab");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        labUI = new DetectionAndTrackingLab();
        window.setContentPane(labUI.mainPanel);
        var ctlSize = labUI.controlPanel.getSize();
        window.setSize(ctlSize.width + frameSize.width, frameSize.height + 30);
        window.setVisible(true);

        labUI.btnExit.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    shutdown();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    System.exit(0);
                }
            });

        });

        labUI.btnSetBackground.addActionListener(e -> {
            //backgroundSubstractor.setBackground(frameSrc);
        });

        labUI.btnSnapshot.addActionListener(e -> {
            synchronized (frameSrc) {
                var timestamp = System.currentTimeMillis();
                Imgcodecs.imwrite("media/image-" + timestamp + ".jpg", frameSrc);
            }
        });

        labUI.btnRecord.addActionListener(e -> {
            if (videoWriter == null) {
                var timestamp = System.currentTimeMillis();
                var size = new Size(frameSize.width, frameSize.height);
                videoWriter = new VideoWriter("media/video-" + timestamp + ".avi", VideoWriter.fourcc('M', 'J', 'P', 'G'), 30, size);
                labUI.btnRecord.setText("Stop");
            } else {
                refreshDetection();
                videoWriter.release();
                videoWriter = null;
                labUI.btnRecord.setText("Record");
            }
        });

    }

    @PreDestroy
    private void shutdown() throws InterruptedException {
        running = false;
        if (pool != null) {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.SECONDS);
        }
        //window.dispose();
        System.out.println("window.dispose");
    }
}
