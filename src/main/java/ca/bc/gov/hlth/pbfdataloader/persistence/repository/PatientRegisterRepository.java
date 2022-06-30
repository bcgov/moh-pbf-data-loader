package ca.bc.gov.hlth.pbfdataloader.persistence.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;

@Repository
public interface PatientRegisterRepository extends CrudRepository<PatientRegister, Long> {

	@Query(value = "UPDATE PatientRegister SET archived = true")
	@Modifying
	void archiveAll();
}
