package org.viar.core.model;

import javax.vecmath.Point2d;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CameraSpaceMarkerPosition {
	
	private @Getter CameraSetup camera;
	private @Getter int markerId;
	private @Getter Point2d position;
	
}