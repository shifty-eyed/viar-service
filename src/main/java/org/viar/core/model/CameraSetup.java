package org.viar.core.model;

import java.util.Arrays;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.viar.core.ConvertUtil;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class CameraSetup {

	@AllArgsConstructor
	public class Intrinsic {
		private @Getter Mat cameraMatrix;
		private @Getter MatOfDouble distCoefficients;
		
		public Point normalizePoint(double x, double y) {
			MatOfPoint2f result = new MatOfPoint2f();
			Calib3d.undistortPoints(new MatOfPoint2f(new Point(x,y)), result, cameraMatrix, distCoefficients);
			return result.toArray()[0];
		}
	}
	
	public class Extrinsic {
		public Extrinsic(Mat rvec, MatOfDouble tvec) {
			this.rvec = rvec;
			this.tvec = tvec;
			rotationMatrix = Mat.zeros(3, 3, CvType.CV_64F);
			Calib3d.Rodrigues(rvec, rotationMatrix);
			
			extrinsicMatrix = Mat.zeros(3, 4, CvType.CV_64F);
			Core.hconcat(Arrays.asList(rotationMatrix, tvec), extrinsicMatrix);
		}
		private @Getter Mat rvec;
		private @Getter MatOfDouble tvec;
		private @Getter Mat rotationMatrix;
		private @Getter Mat extrinsicMatrix;
		
	}
	
	private static void log(String s) {
		System.out.println(s);
	}

	private @Getter int id;
	private @Getter Matrix4d modelView;
	private @Getter Vector3d direction;
	private @Getter Vector3d position;
	private @Getter Intrinsic intrinsic;
	private @Getter Extrinsic extrinsic;
	private @Getter Mat projectionMatrix;
	
	public CameraSetup(int id, Vector3d eye, Vector3d center, Vector3d up, Mat cameraMatrix, MatOfDouble distCoefficients, MatOfDouble rvec, MatOfDouble tvec) {
		this.id = id;
		initLookAt(eye, center, up);
		intrinsic = new Intrinsic(cameraMatrix, distCoefficients);
		extrinsic = new Extrinsic(rvec, tvec);
		
		projectionMatrix = new Mat();
		Core.gemm(cameraMatrix, extrinsic.getExtrinsicMatrix(), 1.0, new Mat(), 0.0, projectionMatrix);
		log("projection #"+id+"\n"+ConvertUtil.stringOfMat(projectionMatrix));
	}
	
	
	public void initLookAt(Vector3d eye, Vector3d center, Vector3d up) {
        Matrix3d rotation = new Matrix3d();
        
        float[] m = new float[16];
        Matrix.setLookAtM(m, 0, (float)eye.x, (float)eye.y, (float)eye.z,
        		(float)center.x, (float)center.y, (float)center.z,
        		(float)up.x, (float)up.y, (float)up.z);
        rotation.setRow(0, m[0], m[4], m[8]);
        rotation.setRow(1, m[1], m[5], m[9]);
        rotation.setRow(2, m[2], m[6], m[10]);
        
        direction = new Vector3d();
        direction.sub(center, eye);
        direction.normalize();
        
        position = eye;
        modelView = new Matrix4d(rotation, position, 1);
	}
	
	public void initLookAtVecMath(Vector3d eye, Vector3d center, Vector3d up) {
		
        Vector3d forward = new Vector3d();
        forward.sub(center, eye);
        forward.normalize();
        log("forward: "+forward);

        Vector3d side = new Vector3d();
        side.cross(forward, up);
        side.normalize();
        log("side: "+side);

        Vector3d upNew = new Vector3d();
        upNew.cross(side, forward);
        upNew.normalize();
        log("upnew: "+upNew);

        Matrix3d rotation = new Matrix3d();
        rotation.setColumn(0, side);
        rotation.setColumn(1, upNew);
        rotation.setColumn(2, forward);
        
        direction = forward;
        position = eye;
        modelView = new Matrix4d(rotation, position, 1);
	}
	
	public static Mat cvCameraMartix(double f, double cx, double cy) {
		Mat result = Mat.zeros(3, 3, CvType.CV_64F);
		result.put(0, 0, f);
		result.put(1, 1, f);
		result.put(0, 2, cx);
		result.put(1, 2, cy);
		result.put(2, 2, 1);
		return result;
	}



}