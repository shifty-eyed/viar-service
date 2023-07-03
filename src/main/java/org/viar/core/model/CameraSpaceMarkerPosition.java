package org.viar.core.model;

import org.opencv.core.Point;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Deprecated
public class CameraSpaceMarkerPosition {
	
	private final int frameWidthHalf = 1920 / 2;  
	private final int frameHeightHalf = 1080 / 2;  
	private final double frameScale = frameWidthHalf;  
	
	private @Getter CameraSetup camera;
	private @Getter int markerId;
	private @Getter Point rawPosition;
	private @Getter Point normalizedPosition;
	
	public CameraSpaceMarkerPosition(CameraSetup camera, int markerId, Point rawPosition) {
		this.camera = camera;
		this.markerId = markerId;
		this.rawPosition = rawPosition;
		
		normalizedPosition = new Point((rawPosition.x - frameWidthHalf) / frameScale,
				(rawPosition.y - frameHeightHalf) / frameScale);
		
	}
	
}