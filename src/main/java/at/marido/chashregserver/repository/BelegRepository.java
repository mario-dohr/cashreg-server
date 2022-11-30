package at.marido.chashregserver.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import at.marido.chashregserver.entity.BelegEntity;

public interface BelegRepository extends JpaRepository<BelegEntity,Long> {

	long countByDatumBetween(LocalDate von, LocalDate bis);
	List<BelegEntity> findAllByDatumBetween(LocalDate von, LocalDate bis, Pageable page);
}
