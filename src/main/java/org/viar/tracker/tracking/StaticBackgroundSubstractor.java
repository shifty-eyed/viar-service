package org.viar.tracker.tracking;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class StaticBackgroundSubstractor {

	private Mat background;
	private Mat mask;

	public StaticBackgroundSubstractor() {
		mask = new Mat();
	}

	public void setBackground(Mat image) {
		background = image.clone();
	}

	public void fillBackground(Mat src, Mat dst, Scalar color) {
		if (background == null) {
			src.copyTo(dst); // do nothing
			return;
		}

		Core.absdiff(src, background, mask);
		Imgproc.threshold(mask, mask, 10, 255, Imgproc.THRESH_BINARY);
		List<Mat> channels = new ArrayList<>(3);

		Core.split(mask, channels);
		Core.bitwise_or(channels.get(0), channels.get(1), mask);
		Core.bitwise_or(channels.get(2), mask, mask);

		Core.bitwise_not(mask, mask);
		Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR);
		mask.copyTo(dst);
		//src.copyTo(dst);
		//dst.setTo(color, mask);
	}

}
