package org.viar.core;

import java.util.Arrays;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ConvertUtil {
	
	public static Mat matrix4dToMat(Matrix4d matrix4d) {
        Mat mat = new Mat(4, 4, CvType.CV_64F);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                mat.put(i, j, matrix4d.getElement(i, j));
            }
        }
        return mat;
    }
	
	public static Mat matrix3dToMat(Matrix3d matrix4d) {
		final int size=3;
		Mat mat = new Mat(size, size, CvType.CV_64F);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				mat.put(i, j, matrix4d.getElement(i, j));
			}
		}
		return mat;
	}
	
	public static String stringOfMat(Mat m) {
		StringBuilder sb = new StringBuilder();
		for (int y=0; y<m.rows(); y++) {
			for (int x=0; x<m.cols(); x++)
				sb.append(Arrays.toString(m.get(y, x))).append(", ");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static String stringOfMatLine(Mat m) {
		StringBuilder sb = new StringBuilder();
		for (int y=0; y<m.rows(); y++) {
			for (int x=0; x<m.cols(); x++)
				sb.append(Arrays.toString(m.get(y, x))).append(", ");
		}
		return sb.toString();
	}
	
	

}
