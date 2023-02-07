package org.viar.core;

import java.util.Arrays;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.junit.Test;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.viar.core.model.CameraSetup;

public class ObjectPositionResolverTest {
	static {
		nu.pattern.OpenCV.loadShared();
	}
	private ObjectPositionResolver testee = new ObjectPositionResolver();
	
	static Mat CameraMatrix = ObjectPositionResolver.cvCameraMartix(2754.05, 2619.06, 301.999, 370.694);
	static MatOfDouble DistCoeffs = new MatOfDouble(-0.312701, 0.8252, -0.0240964, -0.00733861, -0.923225);
	
	static Mat CameraMatrixIdeal = ObjectPositionResolver.cvCameraMartix(1303, 1303, 960, 540);
	static MatOfDouble DistCoeffsIdeal = new MatOfDouble(0, 0, 0, 0, 0);
	
	/*  calibrate camera -> intrinsic
	 *  solvePnP -> extrinsic
	 *  
	 *  undistortPoints 
	 *  
	 *   Rodrigues method to convert the rotation vector to a rotation matrix
	 *   Rodrigues method to convert rvec and tvec to rotation matrix (model matrix)
	 *   
	 *   composeRT method is used to combine the intrinsic and extrinsic parameters to obtain the projection matrix.
	 *   
	 *   projection matrix -> Calib3d.triangulatePoints to recover 3d points
	 */


	@Test
	public void testTriangulate() {
		
		//CameraSetup cam1 = new CameraSetup(0, new Vector3d(1.525,-1.2, 1.2), new Vector3d(-1.425, -1.0125, 1.075), new Vector3d(0,0,1));
		//CameraSetup cam2 = new CameraSetup(0, new Vector3d(-0.4875, -4.05, 1.6875), new Vector3d(-0.35, -1.0375, 1.0), new Vector3d(0,0,1));//tima
		
		
		System.out.println("---Cam1");
		CameraSetup cam1 = new CameraSetup(0, new Vector3d(0, -4, 0), new Vector3d(2,-2, 0), new Vector3d(0,0,1));
		Mat proj1 = composeProjection(cam1, CameraMatrixIdeal);
		
		
		System.out.println("---Cam2");
		CameraSetup cam2 = new CameraSetup(1, new Vector3d(4, -4, 0), new Vector3d(2,-2, 0), new Vector3d(0,0,1));
		Mat proj2 = composeProjection(cam2, CameraMatrixIdeal);
		
		//Point2d point1 = new Point2d(1678, 736);
		//Point2d point2 = new Point2d(895, 618);
		Point2d point1 = new Point2d(0, 0);
		Point2d point2 = new Point2d(0, 0);
		
		
		Point3d v = linearTriangulation(point1, proj1, point2, proj2); 
		
		System.out.println(v.toString());
		
		
	}
	
	/*private Mat composeProjection2(Vector3d rot, Vector3d trans) {
		
		Mat rvec = new Mat(3, 1, CvType.CV_64F);
		rvec.put(0, 0, rot.x);
		rvec.put(1, 0, rot.y);
		rvec.put(2, 0, rot.z);
		Mat tvec = new Mat(3, 1, CvType.CV_64F);
		tvec.put(0, 0, trans.x);
		tvec.put(1, 0, trans.y);
		tvec.put(2, 0, trans.z);
		
		Mat rmat = new Mat(3, 3, CvType.CV_64F);
		
		Calib3d.Rodrigues(rvec, rmat);
		
		System.out.println("extrinsic:");
		System.out.println(stringOfMat(extrinsic));
		
		return extrinsic;
	}*/
	
	private Mat composeProjection(CameraSetup cam, Mat cameraMatrix) {
		Matrix3d rot = new Matrix3d();
		Vector3d trans = new Vector3d();
		
		cam.getModelView().get(rot, trans);
		
		Mat extrinsic = Mat.zeros(3, 4, CvType.CV_64F);
		extrinsic.put(0, 0, rot.m00, rot.m01, rot.m02, trans.x);
		extrinsic.put(1, 0, rot.m10, rot.m11, rot.m12, trans.y);
		extrinsic.put(2, 0, rot.m20, rot.m21, rot.m22, trans.z);
		
		//Mat projection = new Mat();
		//Core.gemm(cameraMatrix, extrinsic, 1.0, new Mat(), 0.0, projection);
		
		//System.out.println("intrinsic:");
		//System.out.println(stringOfMat(cameraMatrix));
		
		System.out.println("extrinsic:");
		System.out.println(stringOfMat(extrinsic));
		
		//System.out.println("projection:");
		//System.out.println(stringOfMat(projection));
		
		return extrinsic;
	}
	
	
	public void solvePnPTest() {
		
		//MatOfPoint3f objectPoints, MatOfPoint2f imagePoints, Mat cameraMatrix, MatOfDouble distCoeffs, Mat rvec, Mat tvec
		
		MatOfPoint3f objectPoints = new MatOfPoint3f(new Point3(-0.7625, 0, 0.875));
		MatOfPoint2f imagePoints = new MatOfPoint2f(new Point(1,2));
				
		Mat outRotation = new Mat();
		Mat outTranslation = new Mat();
		
		Calib3d.solvePnP(objectPoints, imagePoints, CameraMatrix, DistCoeffs, outRotation, outTranslation);

		//System.out.println(v.toString());
	}
	
	private static String stringOfMat(Mat m) {
		StringBuilder sb = new StringBuilder();
		for (int y=0; y<m.rows(); y++) {
			for (int x=0; x<m.cols(); x++)
				sb.append(Arrays.toString(m.get(y, x))).append(", ");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static Point3d linearTriangulation(Point2d p1, Mat projMatrix1, Point2d p2, Mat projMatrix2) {
		Mat points1 = new Mat(2,1,CvType.CV_32F);
		points1.put(0, 0, p1.x);
		points1.put(1, 0, p1.y);
		Mat points2 = new Mat(2,1,CvType.CV_32F);
		points2.put(0, 0, p2.x);
		points2.put(1, 0, p2.y);
		
		Mat resultMat = new Mat();
		Calib3d.triangulatePoints(projMatrix1, projMatrix1, points1, points2, resultMat);
		
		System.out.println("Resultmat:");
		System.out.println(stringOfMat(resultMat));
		
		double w = resultMat.get(3, 0)[0];
		return new Point3d(resultMat.get(0, 0)[0] / w, resultMat.get(1, 0)[0] / w, resultMat.get(2, 0)[0] / w);
	}

}
