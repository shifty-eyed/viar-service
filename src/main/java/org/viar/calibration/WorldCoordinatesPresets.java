package org.viar.calibration;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.springframework.stereotype.Component;

@Component
public class WorldCoordinatesPresets {
	
	private Map<Integer, Double> zCoordByMarkerId;
	
	private Map<String, Point2d> xyCoordByPresetName;
	
	@PostConstruct
	private void init() {
		zCoordByMarkerId = new HashMap<>();
		zCoordByMarkerId.put(0, 0.06985);
		zCoordByMarkerId.put(1, 0.59055);
		zCoordByMarkerId.put(2, 1.050925);
		zCoordByMarkerId.put(3, 1.6002);
		zCoordByMarkerId.put(4, 1.98755);
		
		xyCoordByPresetName = new HashMap<>();
		xyCoordByPresetName.put("a1", new Point2d(1,2));
		xyCoordByPresetName.put("b2", new Point2d(3,4));
		xyCoordByPresetName.put("b3", new Point2d(5,6));
	}
	
	public Point3d getWorldCoords(String presetName, int markerId) {
		Point2d xy = xyCoordByPresetName.get(presetName);
		Double z = zCoordByMarkerId.get(markerId);
		if (xy == null || z == null ) {
			throw new IllegalArgumentException("Coord not found: preset="+presetName+", marker="+markerId);
		} else {
			return new Point3d(xy.x, xy.y, z); 
		}
	}
	
	public String[] getPresetNames() {
		return xyCoordByPresetName.keySet().toArray(new String[xyCoordByPresetName.size()]);
	}
	

}
