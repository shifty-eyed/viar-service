package org.viar.calibration.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
@AllArgsConstructor
public class RawCamerasConfig {
	private Map<String, RawCameraParameters> data;
}