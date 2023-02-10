package org.viar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.stereotype.Component;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceMarkerPosition;
import org.viar.core.model.MarkerNode;

@Component
public class ObjectPositionResolver {
	
	private static final double GOOD_ENOUGH_DOT_THRESHOLD = 0.05; 
	
	private Collection<MarkerNode> nodes;
	private Map<Integer, CameraSetup> cameras;
	
	public static Mat cvCameraMartix(double f, double cx, double cy) {
		Mat result = Mat.zeros(3, 3, CvType.CV_64F);
		result.put(0, 0, f);
		result.put(1, 1, f);
		result.put(0, 2, cx);
		result.put(1, 2, cy);
		result.put(2, 2, 1);
		return result;
	}
	
	
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
	
	void resolveNodePosition(MarkerNode node, List<CameraSpaceMarkerPosition> registerList) {
		assert(registerList.size() >=2);
		
		CameraSpaceMarkerPosition[] bestCandidates = findBestCandidates(registerList);
		Matrix4d m = new Matrix4d(bestCandidates[0].getCamera().getModelView());
		m.mul(bestCandidates[1].getCamera().getModelView());
		
	}
	
	private CameraSpaceMarkerPosition[] findBestCandidates(List<CameraSpaceMarkerPosition> registerList) {
		double bestDot = 1.0;
		CameraSpaceMarkerPosition[] bestCandidates = new CameraSpaceMarkerPosition[2];
		
		for (CameraSpaceMarkerPosition c1 : registerList) {
			for (CameraSpaceMarkerPosition c2 : registerList) {
				if (c1 == c2) {
					break;
				}
				double dot = Math.abs(c1.getCamera().getDirection().dot(c2.getCamera().getDirection()));
				if (bestDot - dot > GOOD_ENOUGH_DOT_THRESHOLD) {
					bestDot = dot;
					bestCandidates[0] = c1;
					bestCandidates[1] = c2;
				} else {
					double maxDistance = Arrays.asList(
						Math.abs(c1.getPosition().x),
						Math.abs(c1.getPosition().y),
						Math.abs(c2.getPosition().x),
						Math.abs(c2.getPosition().y)		
					).stream().collect(Collectors.maxBy(Double::compare)).get();
					
					double maxDistanceBest = Arrays.asList(
							Math.abs(bestCandidates[0].getPosition().x),
							Math.abs(bestCandidates[0].getPosition().y),
							Math.abs(bestCandidates[1].getPosition().x),
							Math.abs(bestCandidates[1].getPosition().y)		
							).stream().collect(Collectors.maxBy(Double::compare)).get();
					if (maxDistance < maxDistanceBest) {
						bestDot = dot;
						bestCandidates[0] = c1;
						bestCandidates[1] = c2;
					}
					
				}
			}
		}
		return bestCandidates;
	}
	
	public void resolve(Map<Integer, Map<Integer, Point2d>> rawData) {
		
	}

}
