package org.viar.tracker;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.viar.tracker.ui.DetectionAndTrackingLab;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Profile("lab")
@Component
public class LabMonitor implements Runnable {

    private final Dimension frameSize = new Dimension(1920, 1080);

    private ExecutorService pool;

    @Setter @Getter
    private boolean running = true;

    private JFrame window;
    private DetectionAndTrackingLab labUI;

    private VideoCapture capture;
    private Mat frameSrc;
    private Mat frameMarkup;


    @PostConstruct
    private void init() {

        capture = new VideoCapture(0, Videoio.CAP_V4L2, new MatOfInt(
                Videoio.CAP_PROP_FOURCC, VideoWriter.fourcc('M', 'J', 'P', 'G'),
                Videoio.CAP_PROP_FRAME_WIDTH, frameSize.width,
                Videoio.CAP_PROP_FRAME_HEIGHT, frameSize.height
        ));

        frameSrc = new Mat();
        frameMarkup = new Mat();

        window = new JFrame("Lab");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        labUI = new DetectionAndTrackingLab();
        window.setContentPane(labUI.mainPanel);
        var ctlSize = labUI.controlPanel.getSize();
        window.setSize(ctlSize.width + frameSize.width, frameSize.height + 30);
        window.setVisible(true);

        labUI.btnExit.addActionListener(e -> {
            running = false;
            pool.shutdown();
            try {
                pool.awaitTermination(200, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            window.dispose();
            System.exit(0);
        });

        //check if the camera is opened and able to capture frames
        if (!capture.isOpened()) {
            System.out.println("Error - cannot open camera");
            JOptionPane.showMessageDialog(null, "Cannot open camera", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } else {
            capture.read(frameSrc);
            if (frameSrc.width() == frameSize.width && frameSrc.height() == frameSize.height) {
                pool = Executors.newSingleThreadExecutor();
                pool.execute(this);
            } else {
                System.out.println("Error - cannot capture requested frame size");
                JOptionPane.showMessageDialog(null, "Cannot capture requested frame size", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

    }

    @Override
    public void run() {
        var frameCount = 0;
        var startTime = System.currentTimeMillis();
        while (running) {
            capture.read(frameSrc);
            frameMarkup = frameSrc.clone();

            processFrame(frameSrc, frameMarkup);

            var javaImage = HighGui.toBufferedImage(frameMarkup);
            labUI.imagePanel.getGraphics().drawImage(javaImage, 0, 0, null);

            frameCount++;
            if (System.currentTimeMillis() - startTime > 1000) {
                labUI.labelFPS.setText(String.format("%d FPS", frameCount));
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }

        capture.release();
    }

    private void processFrame(Mat frameSrc, Mat frameMarkup) {



    }

    @PreDestroy
    private void shutdown() throws InterruptedException {
        capture.release();
        pool.shutdown();
    }
}
