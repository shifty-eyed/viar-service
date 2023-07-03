package org.viar.core.model;

import java.util.List;

import lombok.Data;

@Data
public class CameraSpaceSamples {
	private String cameraName;
	private List<CameraSpaceArucoMarker> arucos;
	private List<CameraSpaceBodyPose> bodies;
}
