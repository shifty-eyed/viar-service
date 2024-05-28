package org.viar.core.model;

import lombok.Data;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import javax.vecmath.Point2d;


@Data
public class CameraSpaceFeature implements Cloneable {

	private String cameraName;
	private String objectName; //or type body1, body2, aruco100
	private int id;

	private double x;
	private double y;

	private double width;
	private double height;

	public CameraSpaceFeature() {
	}

	public CameraSpaceFeature(CameraSpaceFeature other) {
		this.cameraName = other.cameraName;
		this.objectName = other.objectName;
		this.id = other.id;
		this.x = other.x;
		this.y = other.y;
		this.width = other.width;
		this.height = other.height;
	}

	public Rect getRoI() {
		return (width > 0 && height > 0) ? new Rect((int)(x-width/2), (int)(y-height/2), (int)width, (int)height) : null;
	}

	public void setRoI(Rect roi) {
		if (roi != null) {
			x = roi.x + roi.width / 2.0;
			y = roi.y + roi.height / 2.0;
			width = roi.width;
			height = roi.height;
		}
	}
	
	public String getUniqueName() {
		return objectName + "/" + id;
	}

	public Point getCvPoint() {
		return new Point(x, y);
	}

	public Point2d getPoint2d() {
		return new Point2d(x, y);
	}

}
