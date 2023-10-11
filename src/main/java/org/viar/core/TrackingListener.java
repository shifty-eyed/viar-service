package org.viar.core;

import java.util.Collection;

import org.viar.core.model.CameraSpaceFrame;
import org.viar.core.model.WorldSpaceVertex;

public interface TrackingListener {

	void trackingUpdated(Collection<CameraSpaceFrame> rawData, Collection<WorldSpaceVertex> resolved,
			long timeMillis);

}