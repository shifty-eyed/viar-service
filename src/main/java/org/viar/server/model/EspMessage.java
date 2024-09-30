package org.viar.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;

@AllArgsConstructor
@Getter
public abstract class EspMessage {

	public static final byte TYPE_DATA = 1;
	public static final byte TYPE_ACK = 2;
	public static final byte TYPE_TIMESTAMP = 3;


	private short type;
	private long messageNumber;
	private long timestamp;
	private int deviceId;

	public static EspMessage fromBytes(byte[] bytes) {
		ByteBuffer data = ByteBuffer.wrap(bytes);
		var type = data.getShort();
		var messageNumber = data.getLong();
		var timestamp = data.getLong();
		var deviceId = data.getShort();

		switch (type) {
			case TYPE_DATA:
				return new Data(type, messageNumber, timestamp, deviceId, data);
			default:
				throw new IllegalArgumentException("Unknown message type: " + type);
		}
	}

	@Getter
	public static class Data extends EspMessage {

		private final Vector3f acc;

		private final Vector3f gyro;

		private final Vector3f angle;

		private final Vector3f mag;

		public Data(short type, long messageNumber, long timestamp, int deviceId, ByteBuffer data) {
			super(type, messageNumber, timestamp, deviceId);
			this.acc = readVector3f(data);
			this.gyro = readVector3f(data);
			this.angle = readVector3f(data);
			this.mag = readVector3f(data);
		}
	}

	protected Vector3f readVector3f(ByteBuffer data) {
		var x = data.getFloat();
		var y = data.getFloat();
		var z = data.getFloat();
		return new Vector3f(x, y, z);
	}


}
