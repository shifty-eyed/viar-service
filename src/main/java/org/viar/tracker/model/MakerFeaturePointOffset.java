package org.viar.tracker.model;

import lombok.Data;
import org.opencv.core.Point3;

import javax.vecmath.Point3f;

/*
 * This class is used to store the offset of the feature points of a marker.
 * the point coordinates of the feature relative to the marker center.
 */

@Data
public class MakerFeaturePointOffset {
	private int markerId;
	private int featureId;
	private double x;
	private double y;
	private double z;

	public Point3 getPoint3() {
		return new Point3((float)x, (float)y, (float)z);
	}
}
