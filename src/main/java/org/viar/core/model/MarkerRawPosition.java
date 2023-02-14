package org.viar.core.model;

import org.opencv.core.Point;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode(of={"markerId"}, callSuper = false)
public class MarkerRawPosition {
	
	//private @Getter some enum markerType;
	private @Getter int markerId;
	private @Getter Point position;
	
}