package org.viar.calibration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Data
public class CalibrationSample{

	private @Getter double u;
	private @Getter double v;
	private @Getter double x;
	private @Getter double y;
	private @Getter double z;

	@Override
	public String toString() {
		return String.format("(%.0f, %.0f) -> (%.5f, %.5f, %.5f)", u, v, x, y, z);
	}

}