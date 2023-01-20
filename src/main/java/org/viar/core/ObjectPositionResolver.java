package org.viar.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.springframework.stereotype.Component;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceMarkerPosition;
import org.viar.core.model.MarkerNode;

@Component
public class ObjectPositionResolver {
	
	private Collection<MarkerNode> nodes;
	private Map<Integer, CameraSetup> cameras;
	
	
	private MarkerNode getNodeIdByMarkerId(int markerId) {
		for (MarkerNode node : nodes) {
			if (node.getMarkerIds().contains(markerId)) {
				return node;
			}
		}
		throw new IllegalArgumentException("Not found markerId=" + markerId);
	}
	
	public Map<MarkerNode, List<CameraSpaceMarkerPosition>> groupByMarkerNode(Map<Integer, Map<Integer, Point2d>> rawData) {
		
		Map<MarkerNode, List<CameraSpaceMarkerPosition>> result = new HashMap<>();
		
		rawData.entrySet().forEach((cameraToMarkers) -> {
			final int cameraId = cameraToMarkers.getKey();
			Map<Integer, Point2d> markersPositions = cameraToMarkers.getValue();
			
			markersPositions.entrySet().forEach((markerPosition) -> {
				int markerId = markerPosition.getKey();
				Point2d position = markerPosition.getValue();
				MarkerNode node = getNodeIdByMarkerId(markerId);
				
				List<CameraSpaceMarkerPosition> entries = result.get(node);
				if (entries == null) {
					entries = new ArrayList<>();
				}
				entries.add(new CameraSpaceMarkerPosition(cameras.get(cameraId), markerId, position));
			});
			
		});
		
		return result;
	}
	
	public void resolve(Map<Integer, Map<Integer, Point2d>> rawData) {
		
		
		
	}

}
