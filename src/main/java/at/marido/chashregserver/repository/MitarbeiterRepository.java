package at.marido.chashregserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import at.marido.chashregserver.entity.MitarbeiterEntity;

public interface MitarbeiterRepository extends JpaRepository<MitarbeiterEntity,Long>{
	MitarbeiterEntity findByBenutzerId(String benutzerId);

}
