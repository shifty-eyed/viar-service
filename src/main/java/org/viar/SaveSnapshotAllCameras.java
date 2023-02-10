package org.viar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.viar.core.ObjectPositionResolver;

public class SaveSnapshotAllCameras {
	
	private static final int cameras = 2;
	

	public static void main(String[] args) throws Exception {
		nu.pattern.OpenCV.loadShared();
		
		Mat CameraMatrixWin = ObjectPositionResolver.cvCameraMartix(1698.2, 1001.74, 606.883);
		MatOfDouble DistCoeffsWin = new MatOfDouble(0.185377, -1.04121, 0, 0, 1.09319);

		
		VideoCapture[] cam = new VideoCapture[cameras];
		ExecutorService pool = Executors.newFixedThreadPool(cameras);
		
		for (int i=0; i<cameras; i++) {
			System.out.println("Init Camera " + i);
			cam[i] = new VideoCapture(i);
			cam[i].set(3, 1920);
			cam[i].set(4, 1080);
		}
		for (int i=0; i<cameras; i++) {
			final int camId = i;
			pool.submit(() -> {
				System.out.println("Grabbing frame " + camId);
				Mat frame = new Mat();
				cam[camId].read(frame);
				Imgcodecs.imwrite("d:/ws/calib/img-cam"+camId+".png", frame);
				Mat undistort = new Mat();
				Calib3d.undistort(frame, undistort, CameraMatrixWin, DistCoeffsWin);
				Imgcodecs.imwrite("d:/ws/calib/img-cam"+camId+"-fixed.png", undistort);
			});
		}
		pool.shutdown();
	}

}
