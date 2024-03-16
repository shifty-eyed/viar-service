package org.viar.tracker.detection;

import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BodyPoseDetector implements FeatureDetector {

	private static final double THRESHOLD = 0.1;
	private static final int NUM_PARTS = 18;
	private static final Size INPUT_IMAGE_SIZE = new Size(656, 368);
	private static final Scalar INPUT_IMAGE_MEAN = new Scalar(128, 128, 128);

	private static final String NET_BIN_FILE = "models/detect-body-pose/graph_opt_thin.pb";
	private static final String NET_TXT_FILE = "models/detect-body-pose/graph_opt_thin.pbtxt";
	private static final boolean USE_GPU = false;

	private final Net net;
	private Mat output;

	public BodyPoseDetector() {
		net = Dnn.readNetFromTensorflow(NET_BIN_FILE, NET_TXT_FILE);
		if (USE_GPU) {
			net.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
			net.setPreferableTarget(Dnn.DNN_BACKEND_CUDA);
		}
	}

	@Override
	public Collection<CameraSpaceFeature> detect(Mat frame, CameraSetup cameraSetup) {
		Mat inputBlob = Dnn.blobFromImage(frame, 0.5,
			INPUT_IMAGE_SIZE,
			INPUT_IMAGE_MEAN, false, false);
		net.setInput(inputBlob);//1*3*368*656* CV32FC1
		output = net.forward();
		// the result is an array of "heatmaps", the probability of a body part being in location x,y



		var h = output.size(2);
		var w = output.size(3);
		var sx = frame.cols() / (float) w;
		var sy = frame.rows() / (float) h;

		// find the position of the body parts 1*57*46*82
		List<CameraSpaceFeature> features = new ArrayList<>(NUM_PARTS);
		for (int i = 0; i < NUM_PARTS; i++) {
			Mat heatMap = output.col(i).reshape(1, h);
			Core.MinMaxLocResult mm = Core.minMaxLoc(heatMap);
			if (mm.maxVal > THRESHOLD) {
				features.add(makeFeature(i, mm.maxLoc.x * sx, mm.maxLoc.y * sy, cameraSetup));
			}
		}
		return features;
	}

	public void drawHeatMaps(Mat frame, int numParts) {
		for (int i = 0; i < numParts; i++) {
			Mat heatMap = output.col(i).reshape(1, output.size(2));
			// jusr draw the heatmap on frame for debug
			Imgproc.resize(heatMap, heatMap, frame.size(), 0, 0, Imgproc.INTER_NEAREST_EXACT);
			heatMap.convertTo(heatMap, CvType.CV_8U, 255);
			Imgproc.applyColorMap(heatMap, heatMap, Imgproc.COLORMAP_BONE);
			Core.addWeighted(frame, 0.5, heatMap, 0.5, 0, frame);
		}
	}

	private CameraSpaceFeature makeFeature(int id, double x, double y, CameraSetup cameraSetup) {
				CameraSpaceFeature feature = new CameraSpaceFeature();
				feature.setX(x);
				feature.setY(y);
				feature.setId(id);
				feature.setCameraName(cameraSetup.getName());
				feature.setObjectName("BodyPart");
		return feature;
	}
}
