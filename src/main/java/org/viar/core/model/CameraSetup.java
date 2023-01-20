package org.viar.core.model;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CameraSetup {
	
	private @Getter int id;
	private @Getter Matrix4d modelView;
	private @Getter Vector3d direction;
	
	public CameraSetup(int id, Vector3d position, Vector3d target, Vector3d up) {
		direction = new Vector3d();
		direction.sub(target, position);
		direction.normalize();
		
		Vector3d s = new Vector3d();
		s.cross(direction, up);
		s.normalize();
		
		Vector3d u = new Vector3d();
		u.cross(s, direction);
		u.normalize();


		double[] data = new double[16];
		data[0] = s.x;
		data[4] = s.y;
		data[8] = s.z;

		data[1] = u.x;
		data[5] = u.y;
		data[9] = u.z;

		data[2] = -direction.x;
		data[6] = -direction.y;
		data[10] = -direction.z;

		data[3] = 0.0f;
		data[7] = 0.0f;
		data[11] = 0.0f;

		data[12] = - s.dot(position);
		data[13] = - u.dot(position);
		data[14] = direction.dot(position);
		data[15] = 1.0f;
		
		modelView = new Matrix4d(data);
		this.id = id;
		
	}
	
}