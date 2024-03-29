package org.viar.calibration.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
@Data
public class CameraSamplesSet {

	private String cameraName;
	private List<CalibrationSample> calibrationSamples;
}