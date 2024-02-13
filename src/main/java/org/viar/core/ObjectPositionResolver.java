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
import org.viar.core.model.CameraSpaceArucoMarker;
import org.viar.core.model.CameraSpaceBodyPose;
import org.viar.core.model.CameraSpaceFrame;
import org.viar.core.model.CameraSpaceVertex;
import org.viar.core.model.WorldSpaceVertex;

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
	
	private WorldSpaceVertex resolveWorldSpaceVertex(List<CameraSpaceVertex> registerList) {
		CameraSpaceVertex[] stereoPair = findStereoPair(registerList);
		if (stereoPair == null) {
			return null;
		}
		
		CameraSpaceVertex v1 = stereoPair[0];
		CameraSpaceVertex v2 = stereoPair[1];
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
		return new WorldSpaceVertex(v1.getObject(), v1.getId(), (x / w) / inch, (y / w) / inch, (z / w) / inch);
	}
	
	static void log(String msg) {
		System.out.println(msg);
	}
	
	private CameraSpaceVertex[] findStereoPair(List<CameraSpaceVertex> registerList) {
		double bestDot = 1.0;
		CameraSpaceVertex[] bestCandidates = new CameraSpaceVertex[2];
		boolean found = false;
		
		for (CameraSpaceVertex v1 : registerList) {
			for (CameraSpaceVertex v2 : registerList) {
				if (v1 == v2) {
					break;
				}
				//to find best candidates for stereo pair need to take into account not just direction vector from camera position
				//but also pixel coordinates and how far are they from the center
				
				CameraSetup c1 = camerasConfig.get(v1.getCameraName());
				CameraSetup c2 = camerasConfig.get(v2.getCameraName());
				
				double dot = Math.abs(c1.getDirection().dot(c2.getDirection()));
				if (bestDot - dot > GOOD_ENOUGH_DOT_THRESHOLD) {//this is rough way
					bestDot = dot;
					bestCandidates[0] = v1;
					bestCandidates[1] = v2;
					found = true;
				}
			}
		}
		return found ? bestCandidates : null;
	}
	
	private Map<Object, List<CameraSpaceVertex>> groupCameraSpaceVertices(Collection<CameraSpaceFrame> rawData) {
		Collection<CameraSpaceVertex> result = new ArrayList<>();
		for (CameraSpaceFrame frame : rawData) {
			for (CameraSpaceArucoMarker aruco : frame.getArucos()) {
				//TODO: aruco conversion to markerNode, use single camera aruco offset maybe on previous step
				// to convert aruco 4 corners and normal vector to single point but not necessary in the middle of aruco marker  
			}
			for (CameraSpaceBodyPose body : frame.getBodies()) {
				int i = 0; 
				for (Point p : body.getPoints()) {
					CameraSpaceVertex v = new CameraSpaceVertex();
					v.setCameraName(frame.getCameraName());
					v.setObject("body" + body.getId());
					v.setId(i++);
					v.setX(p.x);
					v.setY(p.y);
					result.add(v);
				}
				
			}
		}
		return result.stream().collect(Collectors.groupingBy(v -> v.getUniqueName()));
	}
	
	public Collection<WorldSpaceVertex> resolve(Collection<CameraSpaceFrame> rawData) {
		Collection<WorldSpaceVertex> result = new ArrayList<>();
		
		Map<Object, List<CameraSpaceVertex>> cameraSpaceNodes = groupCameraSpaceVertices(rawData);
		
		for (List<CameraSpaceVertex> cameraVertices : cameraSpaceNodes.values()) {
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
