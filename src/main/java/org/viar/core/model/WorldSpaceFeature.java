package org.viar.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;



@Data
@AllArgsConstructor
public class WorldSpaceFeature {
	
	private String objectName; //or type body1, body2, aruco100
	private int id;
	
	private double x;
	private double y;
	private double z;
	
}
