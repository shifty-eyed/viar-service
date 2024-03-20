package org.viar.core.model;

import lombok.Data;
import org.opencv.core.Rect;


@Data
public class CameraSpaceFeature {

	private String cameraName;
	private String objectName; //or type body1, body2, aruco100
	private int id;

	private double x;
	private double y;

	private double width;
	private double height;

	public Rect getRoI() {
		return (width > 0 && height > 0) ? new Rect((int)(x-width/2), (int)(y-height/2), (int)width, (int)height) : null;
	}
	
	public String getUniqueName() {
		return objectName + "/" + id;
	}
	
}
