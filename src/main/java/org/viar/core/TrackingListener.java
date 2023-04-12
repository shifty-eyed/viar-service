package org.viar.core;

import java.util.Collection;
import java.util.Map;

import javax.vecmath.Point3d;

import org.viar.core.model.MarkerNode;
import org.viar.core.model.MarkerRawPosition;

public interface TrackingListener {

	void trackingUpdated(Map<String, Collection<MarkerRawPosition>> rawData, Map<MarkerNode, Point3d> resolved,
			long timeMillis);

}