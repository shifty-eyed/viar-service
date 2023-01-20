package org.viar.core.model;

import java.util.HashMap;
import java.util.Set;

import javax.vecmath.Vector3d;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode(of={"id"}, callSuper = false)
public class MarkerNode extends HashMap<Integer, Vector3d> {
	private static final long serialVersionUID = 1L;
	
	private @Getter int id;
	
	public Set<Integer> getMarkerIds() {
		return keySet();
	}
	
	public Vector3d getMarkerPosition(int markerId) {
		return get(markerId);
	}
}