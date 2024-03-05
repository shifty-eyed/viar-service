package org.viar.tracker.model;

import java.util.List;

import org.opencv.core.Point;

import lombok.Data;

@Data
public class CameraSpaceBodyPose {
	private int id;
	private List<Point> points;
	
}
