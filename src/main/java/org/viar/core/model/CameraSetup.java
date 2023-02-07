package org.viar.core.model;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.viar.core.ConvertUtil;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CameraSetup {
	
	private static void log(String s) {
		System.out.println(s);
	}

	private @Getter int id;
	private @Getter Matrix4d modelView;
	private @Getter Vector3d direction;
	private @Getter Vector3d position;
	private @Getter Vector3d rvec;

	public CameraSetup(int id, Vector3d eye, Vector3d center, Vector3d up) {
		
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
        //rotation.setColumn(0, side);
        //rotation.setColumn(1, upNew);
        //rotation.setColumn(2, forward);
        
        float[] m = new float[16];
        Matrix.setLookAtM(m, 0, (float)eye.x, (float)eye.y, (float)eye.z,
        		(float)center.x, (float)center.y, (float)center.z,
        		(float)up.x, (float)up.y, (float)up.z);
        rotation.setRow(0, m[0], m[4], m[8]);
        rotation.setRow(1, m[1], m[5], m[9]);
        rotation.setRow(2, m[2], m[6], m[10]);
        
        
        direction = forward;
        position = eye;
        modelView = new Matrix4d(rotation, position, 1);
        
        AxisAngle4d aa = new AxisAngle4d();
        aa.set(rotation);
        log("aa: \n" + aa.toString());
        
        Mat rvec = new Mat(3, 1, CvType.CV_64F);
        Calib3d.Rodrigues(ConvertUtil.matrix3dToMat(rotation), rvec);
        log("rvec: \n" + ConvertUtil.stringOfMat(rvec));
	}


}