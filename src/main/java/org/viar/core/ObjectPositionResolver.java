package org.viar.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.viar.core.model.CameraSetup;
import org.viar.tracker.model.CameraSpaceArucoMarker;
import org.viar.tracker.model.CameraSpaceBodyPose;
import org.viar.core.model.CameraSpaceFeature;
import org.viar.core.model.WorldSpaceFeature;

@Component
public class ObjectPositionResolver {
	
	private static final double GOOD_ENOUGH_DOT_THRESHOLD = 0.05; //get rid of it

	
	@Autowired
	private Map<String, CameraSetup> camerasConfig;
	
	@PostConstruct
	private void init() {
		/*MarkerNode n = new MarkerNode("node");
		for (int i=0; i<90; i++) {
			n.put(i, new Vector3d(0,0,0));
		}
		nodes.add(n);*/
			
	}
	
	private WorldSpaceFeature resolveWorldSpaceVertex(List<CameraSpaceFeature> registerList) {
		CameraSpaceFeature[] stereoPair = findStereoPair(registerList);
		if (stereoPair == null) {
			return null;
		}
		
		CameraSpaceFeature v1 = stereoPair[0];
		CameraSpaceFeature v2 = stereoPair[1];
		CameraSetup c1 = camerasConfig.get(v1.getCameraName());
		CameraSetup c2 = camerasConfig.get(v2.getCameraName());
		
		Mat projMatrix1 = c1.getProjectionMatrix();
		Mat projMatrix2 = c2.getProjectionMatrix();
		
		MatOfPoint2f imagePoint1 = new MatOfPoint2f(new Point(v1.getX(), v1.getY()));
		MatOfPoint2f imagePoint2 = new MatOfPoint2f(new Point(v2.getX(), v2.getY()));
		
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
		
		//return new Point3d((x / w) / inch, (y / w) / inch, (z / w) / inch);
		return new WorldSpaceFeature(v1.getObjectName(), v1.getId(), (x / w) / inch, (y / w) / inch, (z / w) / inch);
	}
	
	static void log(String msg) {
		System.out.println(msg);
	}
	
	private CameraSpaceFeature[] findStereoPair(List<CameraSpaceFeature> registerList) {
		double bestDot = 1.0;
		CameraSpaceFeature[] bestCandidates = new CameraSpaceFeature[2];
		boolean found = false;
		
		for (CameraSpaceFeature v1 : registerList) {
			for (CameraSpaceFeature v2 : registerList) {
				if (v1 == v2) {
					break;
				}
				//to find best candidates for stereo pair need to take into account not just direction vector from camera position
				//but also pixel coordinates and how far are they from the center
				
				CameraSetup c1 = camerasConfig.get(v1.getCameraName());
				CameraSetup c2 = camerasConfig.get(v2.getCameraName());
				
				double dot = Math.abs(c1.getDirection().dot(c2.getDirection()));
				if (bestDot - dot > GOOD_ENOUGH_DOT_THRESHOLD) { //this is a rough way
					bestDot = dot;
					bestCandidates[0] = v1;
					bestCandidates[1] = v2;
					found = true;
				}
			}
		}
		return found ? bestCandidates : null;
	}
	
	public Collection<WorldSpaceFeature> resolve(Collection<CameraSpaceFeature> rawData) {
		Collection<WorldSpaceFeature> result = new ArrayList<>();
		
		Map<Object, List<CameraSpaceFeature>> groupedByFeatureId =
				rawData.stream().collect(Collectors.groupingBy(v -> v.getUniqueName()));
		
		for (List<CameraSpaceFeature> cameraVertices : groupedByFeatureId.values()) {
			// if there is only one camera that sees the feature, we can't resolve its position
			if (cameraVertices.size() < 2) {
				continue;
			}
			final var position = resolveWorldSpaceVertex(cameraVertices);
			if (position != null) {
				result.add(position);
			}
		}
		return result;
	}

}
