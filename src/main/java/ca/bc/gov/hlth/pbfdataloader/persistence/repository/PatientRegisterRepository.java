package ca.bc.gov.hlth.pbfdataloader.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;

@Repository
public interface PatientRegisterRepository extends JpaRepository<PatientRegister, Long> {

	@Query(value = "UPDATE PatientRegister SET archived = true")
	@Modifying
	void archiveAll();
	
	@Query(value = "DELETE FROM PatientRegister WHERE archived = true")
    @Modifying
	void deleteArchived();
	
	@Query(value = "UPDATE PatientRegister SET archived = false WHERE archived = true")
    @Modifying
	void restoreArchived();
	
	@Query(value = "DELETE FROM PatientRegister WHERE archived = false")
    @Modifying
	void deleteNew();
}
