package at.marido.chashregserver.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Mitarbeiter")
public class MitarbeiterEntity extends EntityBase {
	
	@Column(name = "benutzer_id")
	private String benutzerId;

	@Column(name = "vorname")
	private String vorname;

	@Column(name = "nachname")
	private String nachname;

}
