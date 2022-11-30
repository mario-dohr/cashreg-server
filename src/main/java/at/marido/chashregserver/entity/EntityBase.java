package at.marido.chashregserver.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class EntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
