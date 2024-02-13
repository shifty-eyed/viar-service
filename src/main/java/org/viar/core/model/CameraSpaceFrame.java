package org.viar.core.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class CameraSpaceFrame {

	private String cameraName;

	@EqualsAndHashCode.Exclude
	private List<CameraSpaceArucoMarker> arucos;
	@EqualsAndHashCode.Exclude
	private List<CameraSpaceBodyPose> bodies;


}
