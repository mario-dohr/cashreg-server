package at.marido.chashregserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import at.marido.chashregserver.entity.BelegEntity;

public interface BelegRepository extends JpaRepository<BelegEntity,Long> {

}
