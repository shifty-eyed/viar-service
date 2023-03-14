package org.viar.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.core.model.CameraSetup;
import org.viar.core.model.CameraSpaceMarkerPosition;
import org.viar.core.model.MarkerNode;
import org.viar.core.model.MarkerRawPosition;

@Component
public class ObjectPositionResolver {
	
	private static final double GOOD_ENOUGH_DOT_THRESHOLD = 0.05; //get rid of it

	
	static Mat CameraMatrixWin = CameraSetup.cvCameraMartix(1698.2, 1001.74, 606.883);
	static MatOfDouble DistCoeffsWin = new MatOfDouble(0.185377, -1.04121, 0, 0, 1.09319);
	
	static Mat CameraMatrixTim = CameraSetup.cvCameraMartix(1763.01, 972.898, 440.798);
	static MatOfDouble DistCoeffsTim = new MatOfDouble(0.215209, -1.3063, 0, 0, 1.56173);
	
	@Autowired
	private Collection<MarkerNode> nodes;
	
	@Autowired
	private Map<String, CameraSetup> camerasConfig;
	
	@PostConstruct
	private void init() {
		MarkerNode n = new MarkerNode("head");
		for (int i=0; i<10; i++)
			n.put(i, new Vector3d(0,0,0));
		nodes = Arrays.asList(n);
		camerasConfig = new HashMap<>(2);
		camerasConfig.put("0", new CameraSetup("0", 0, new Vector3d(1.5,-1.1,1.5), new Vector3d(0,-1.1,1.5), new Vector3d(0,0,1), 
				CameraMatrixWin, DistCoeffsWin,
				new MatOfDouble(1.931, 0.0205, -0.01434), new MatOfDouble(-0.03032, 0.6279016, 3.0672747)));
		camerasConfig.put("1", new CameraSetup("1", 1, new Vector3d(0,-2.5,1.5), new Vector3d(0,0,1.5), new Vector3d(0,0,1), 
				CameraMatrixTim, DistCoeffsTim,
				new MatOfDouble(1.3005590524914123, 1.1859894659, -1.100206455382724), new MatOfDouble(1.109858441, 1.34952034, 1.83912425)));
		
	}
	
	private MarkerNode getNodeIdByMarkerId(int markerId) {
		for (MarkerNode node : nodes) {
			if (node.getMarkerIds().contains(markerId)) {
				return node;
			}
		}
		return null;
		//throw new IllegalArgumentException("Not found markerId=" + markerId);
	}
	
	private Map<MarkerNode, List<CameraSpaceMarkerPosition>> groupByMarkerNode(Map<String, Collection<MarkerRawPosition>> rawData) {

		Map<MarkerNode, List<CameraSpaceMarkerPosition>> result = new HashMap<>();
		
		rawData.entrySet().forEach((cameraToMarkers) -> {
			final String cameraId = cameraToMarkers.getKey();
			
			cameraToMarkers.getValue().forEach((markerRawPosition) -> {
				MarkerNode node = getNodeIdByMarkerId(markerRawPosition.getMarkerId());
				if (node != null) {
					List<CameraSpaceMarkerPosition> entries = result.get(node);
					if (entries == null) {
						entries = new ArrayList<>();
						result.put(node, entries);
					}
					entries.add(new CameraSpaceMarkerPosition(camerasConfig.get(cameraId), markerRawPosition.getMarkerId(), markerRawPosition.getPosition()));
				}
			});
			
		});
		
		return result;
	}
	
	private Point3d resolveNodePosition(MarkerNode node, List<CameraSpaceMarkerPosition> registerList) {
		CameraSpaceMarkerPosition[] stereoPair = findStereoPair(registerList);
		if (stereoPair == null) {
			return new Point3d();
		}
		Mat projMatrix1 = stereoPair[0].getCamera().getProjectionMatrix();
		Mat projMatrix2 = stereoPair[1].getCamera().getProjectionMatrix();
		
		MatOfPoint2f imagePoint1 = new MatOfPoint2f(stereoPair[0].getRawPosition());
		MatOfPoint2f imagePoint2 = new MatOfPoint2f(stereoPair[1].getRawPosition());
		
		Mat resultMat = new Mat();
		Calib3d.triangulatePoints(projMatrix1, projMatrix2, imagePoint1, imagePoint2, resultMat);
		
		/*log("projMatrix1:");
		log(ConvertUtil.stringOfMat(projMatrix1));
		log("projMatrix2:");
		log(ConvertUtil.stringOfMat(projMatrix2));
		log("imagePoint1:");
		log(ConvertUtil.stringOfMatLine(imagePoint1));
		log("imagePoint2:");
		log(ConvertUtil.stringOfMatLine(imagePoint2));*/
		
		double x = resultMat.get(0, 0)[0];
		double y = resultMat.get(1, 0)[0];
		double z = resultMat.get(2, 0)[0];
		double w = resultMat.get(3, 0)[0];
		
		final double inch = 0.0254;
		
		return new Point3d((x / w) / inch, (y / w) / inch, (z / w) / inch);
		
	}
	
	static void log(String msg) {
		System.out.println(msg);
	}
	
	private CameraSpaceMarkerPosition[] findStereoPair(List<CameraSpaceMarkerPosition> registerList) {
		double bestDot = 1.0;
		CameraSpaceMarkerPosition[] bestCandidates = new CameraSpaceMarkerPosition[2];
		boolean found = false;
		
		for (CameraSpaceMarkerPosition c1 : registerList) {
			for (CameraSpaceMarkerPosition c2 : registerList) {
				if (c1 == c2) {
					break;
				}
				//to find best candidates for stereo pair need to take into account not just direction vector from camera position
				//but also pixel coordinates and how far are they from the center
				double dot = Math.abs(c1.getCamera().getDirection().dot(c2.getCamera().getDirection()));
				if (bestDot - dot > GOOD_ENOUGH_DOT_THRESHOLD) {//this is rough way
					bestDot = dot;
					bestCandidates[0] = c1;
					bestCandidates[1] = c2;
					found = true;
				}
			}
		}
		return found ? bestCandidates : null;
	}
	
	public Map<MarkerNode, Point3d> resolve(Map<String, Collection<MarkerRawPosition>> rawData) {
		Map<MarkerNode, Point3d> result = new HashMap<>();
		
		Map<MarkerNode, List<CameraSpaceMarkerPosition>> byNode = groupByMarkerNode(rawData);
		for (Map.Entry<MarkerNode, List<CameraSpaceMarkerPosition>> e : byNode.entrySet()) {
			result.put(e.getKey(), resolveNodePosition(e.getKey(), e.getValue()));
		}
		return result;
	}

}
