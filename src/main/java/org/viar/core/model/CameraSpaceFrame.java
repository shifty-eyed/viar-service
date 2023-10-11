package org.viar.core.model;

import java.util.List;

import lombok.Data;

@Data
public class CameraSpaceFrame {
	private String cameraName;
	private List<CameraSpaceArucoMarker> arucos;
	private List<CameraSpaceBodyPose> bodies;
}
