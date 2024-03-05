package org.viar.core;

import java.util.Collection;

import org.viar.core.model.CameraSpaceFeature;
import org.viar.core.model.WorldSpaceFeature;

public interface TrackingListener {

	void trackingUpdated(Collection<CameraSpaceFeature> rawData, Collection<WorldSpaceFeature> resolved,
						 long timeMillis);

}