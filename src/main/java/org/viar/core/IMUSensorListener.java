package org.viar.core;

import org.viar.server.model.EspMessage;

import javax.vecmath.Vector3f;

public interface IMUSensorListener {
	void onSensorData(EspMessage.Data data, int messagesPerSecond);
}
