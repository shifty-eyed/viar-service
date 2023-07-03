package org.viar.core.model;

import java.util.List;

import org.opencv.core.Point;

import lombok.Data;

@Data
public class CameraSpaceArucoMarker {
	private int id;
	private double x;
	private double y;
	private List<Point> corners;
	
}
