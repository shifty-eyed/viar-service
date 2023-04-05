package org.viar.calibration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.springframework.stereotype.Component;

@Component
public class WorldCoordinatesPresets {
	
	private Map<Integer, Double> zCoordByMarkerId;
	
	private Map<String, Point2d> xyCoordByPresetName;
	
	private static Point2d f2m(double x, double y) {
		final double multiplier = 0.3048;
		return new Point2d(x * multiplier, y * multiplier);
	}
	
	@PostConstruct
	private void init() {
		zCoordByMarkerId = new HashMap<>();
		zCoordByMarkerId.put(0, 0.06985);
		zCoordByMarkerId.put(1, 0.59055);
		zCoordByMarkerId.put(2, 0.9652);
		zCoordByMarkerId.put(3, 1.397);
		zCoordByMarkerId.put(4, 1.793875);
		
		xyCoordByPresetName = new LinkedHashMap<>();
		xyCoordByPresetName.put("a: 2,-1", f2m(2, -1));
		xyCoordByPresetName.put("b: 3,-1", f2m(3, -1));
		xyCoordByPresetName.put("c: 4,-1", f2m(4, -1));
		xyCoordByPresetName.put("d: 5,-1", f2m(5, -1));
		xyCoordByPresetName.put("e: 6,-1", f2m(6, -1));
		
		xyCoordByPresetName.put("f: 2,-2", f2m(2, -2));
		xyCoordByPresetName.put("g: 3,-2", f2m(3, -2));
		xyCoordByPresetName.put("h: 4,-2", f2m(4, -2));
		xyCoordByPresetName.put("i: 5,-2", f2m(5, -2));
		xyCoordByPresetName.put("j: 6,-2", f2m(6, -2));
		
		xyCoordByPresetName.put("k: 2,-3", f2m(2, -3));
		xyCoordByPresetName.put("l: 3,-3", f2m(3, -3));
		xyCoordByPresetName.put("m: 4,-3", f2m(4, -3));
		xyCoordByPresetName.put("n: 5,-3", f2m(5, -3));
		xyCoordByPresetName.put("o: 6,-3", f2m(6, -3));
		
		xyCoordByPresetName.put("p: 3,-4", f2m(3, -4));
		xyCoordByPresetName.put("q: 4,-4", f2m(4, -4));
		xyCoordByPresetName.put("r: 5,-4", f2m(5, -4));
		xyCoordByPresetName.put("s: 6,-4", f2m(6, -4));
		
		
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
