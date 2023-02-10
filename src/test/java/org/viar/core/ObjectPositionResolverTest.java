package org.viar.core;

import java.util.Arrays;

import javax.vecmath.Point3d;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

public class ObjectPositionResolverTest {
	static {
		nu.pattern.OpenCV.loadShared();
	}
	private ObjectPositionResolver testee = new ObjectPositionResolver();
	
	static final double inch = 0.0254;
	
	static final double[][] calibrationData = {
			//cap1
			{-26.5,-9,27.1,    536,668,  1561,884}, //1
			{-26.5,-20,27.1,   484,644,  1367,888}, //2
			{-42.6,-20,27.1,   174,743,  1290,809}, //3
			{-26.5,-9,46.87,   516,333,  1586,532}, //4
			{-26.5,-20,46.87,  462,367,  1383,531}, //5
			{-35.5,-20,46.87,  279,368,  1338,515}, //6
			
			//cap2
			{17,-20.5,46.87,   1297,372, 1866,703}, //4
			{17,-31.87,46.87,  1352,423, 1470,708}, //5
			{8,-31.87,46.87,   1146,421, 1374,649}, //6
			
			//cap3
			{-14.2,-47,46.87,   546,531, 876,565}, //4
			{-14.2,-58,46.87,   448,653, 625,566}, //5
			{-14.2-9,-58,46.87, 114,648, 653,543}, //6
	};
	
	static void log(String msg) {
		System.out.println(msg);
	}
	
	static final double d45 = Math.PI/4; 
	static final double d90 = Math.PI/2; 
	
	static Mat CameraMatrixWin = ObjectPositionResolver.cvCameraMartix(1698.2, 1001.74, 606.883);
	static MatOfDouble DistCoeffsWin = new MatOfDouble(0.185377, -1.04121, 0, 0, 1.09319);
	
	static Mat CameraMatrixTim = ObjectPositionResolver.cvCameraMartix(1763.01, 972.898, 440.798);
	static MatOfDouble DistCoeffsTim = new MatOfDouble(0.215209, -1.3063, 0, 0, 1.56173);
	
	static Mat CameraMatrixIdeal = ObjectPositionResolver.cvCameraMartix(600, 960, 540);
	static MatOfDouble DistCoeffsIdeal = new MatOfDouble(0, 0, 0, 0, 0);

	
	MatOfPoint3f getObjectPoints(double[][] data) {
		Point3[] result = new Point3[data.length];
		for (int i=0; i<data.length; i++) {
			result[i] = new Point3(data[i][0]*inch, data[i][1]*inch, data[i][2]*inch);
		}
		return new MatOfPoint3f(result);
	}
	
	MatOfPoint2f getImagePoints(int camId, double[][] data) {
		final int offset = 3 + camId;
		Point[] result = new Point[data.length];
		for (int i=0; i<data.length; i++) {
			double x = data[i][offset + 0];
			double y = data[i][offset + 1];
			
			result[i] = new Point(x, y);
		}
		return new MatOfPoint2f(result);
	}
	
	@Test
	public void testTriangulate() {
		try {
			
			//Mat tvec1 = new MatOfPoint3f(new Point3(-0.030323800567347008, 0.6279016588806708, 3.0672747359890327));
			//Mat tvec2 = new MatOfPoint3f(new Point3(0.28126003762961876, 2.04519503497205, 1.9712407528621971));
			
			Mat tvec1 = new MatOfPoint3f(new Point3(-0.030323800567347008, 0.6279016588806708, 1.2));
			Mat tvec2 = new MatOfPoint3f(new Point3(0.28126003762961876, 2.04519503497205, 1.6));
			
			Mat proj1 = composeProjection(0, CameraMatrixWin, DistCoeffsWin);
			Mat proj2 = composeProjection(1, CameraMatrixTim, DistCoeffsTim);
			
			MatOfPoint2f imagePoint1 = getImagePoints(0, calibrationData);
			MatOfPoint2f imagePoint2 = getImagePoints(1, calibrationData);
			
			for (int i=0; i< imagePoint1.rows();i++) {
				Point3d v = linearTriangulation(proj1,proj2, new Point(imagePoint1.get(i, 0)),  
						new Point(imagePoint2.get(i, 0)));
				log("v="+v);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Mat composeProjection(int camId, Mat cameraMatrix, MatOfDouble distCoefficients) {
		log("Camera#"+(camId+1));
		MatOfPoint3f objectPoints = getObjectPoints(calibrationData);
		MatOfPoint2f imagePoints = getImagePoints(camId, calibrationData);
		
		Mat rvec = new Mat();
		Mat tvec = new Mat();
		Calib3d.solvePnP(objectPoints, imagePoints, cameraMatrix, distCoefficients, rvec, tvec);
		
		log("rvec: "+ConvertUtil.stringOfMatLine(rvec));
		log("tvec: "+ConvertUtil.stringOfMatLine(tvec));
		
		Mat rmat = Mat.zeros(3, 3, CvType.CV_64F);
		Calib3d.Rodrigues(rvec, rmat);
		
		log("rmat: \n"+ConvertUtil.stringOfMat(rmat));
		
		Mat extrinsic = Mat.zeros(3, 4, CvType.CV_64F);
		Core.hconcat(Arrays.asList(rmat, tvec), extrinsic);
		
		log("extrinsic: \n"+ConvertUtil.stringOfMat(extrinsic));
		
		Mat projection = new Mat();
		Core.gemm(cameraMatrix, extrinsic, 1.0, new Mat(), 0.0, projection);
		
		log("projection: \n"+ConvertUtil.stringOfMat(projection));
		
		return projection;
	}
	
	public static Point3d linearTriangulation(Mat projMatrix1, Mat projMatrix2, Point p1, Point p2) {
		Mat points1 = new MatOfPoint2f(p1);
		Mat points2 = new MatOfPoint2f(p2);
		
		Mat resultMat = new Mat();
		Calib3d.triangulatePoints(projMatrix1, projMatrix2, points1, points2, resultMat);
		
		log("Resultmat:");
		log(ConvertUtil.stringOfMatLine(resultMat));
		
		double x = resultMat.get(0, 0)[0];
		double y = resultMat.get(1, 0)[0];
		double z = resultMat.get(2, 0)[0];
		double w = resultMat.get(3, 0)[0];
		
		return new Point3d((x / w) / inch, (y / w) / inch, (z / w) / inch);
	}

}
