package org.viar.calibration.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
@AllArgsConstructor
public class CalibrationData {
	private List<CameraSamplesSet> calibrationData;
}