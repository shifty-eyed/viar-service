package org.viar.core.model;

import lombok.Data;



@Data
public class CameraSpaceVertex {
	
	private String cameraName;
	private String group; //or type body1, body2, aruco100
	private int id;
	
	private double x;
	private double y;
	
	public String getUniqueName() {
		return group + "/" + id;
	}
	
}
