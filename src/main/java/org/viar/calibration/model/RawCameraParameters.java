package org.viar.calibration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
@Data
public class RawCameraParameters {

	private double[] rvec;
	private double[] tvec;

}