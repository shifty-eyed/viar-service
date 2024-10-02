package org.viar;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.viar.tracker.detection.BodyPoseDetector;

import javax.swing.*;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());

		System.loadLibrary("opencv_java490");
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
		builder.headless(false);
		builder.run(args);
	}


}
