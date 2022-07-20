package ca.bc.gov.hlth.pbfdataloader.persistence.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;

@Repository
public interface PBFClinicPayeeRepository extends CrudRepository<PBFClinicPayee, Long> {
	
	@Query(value = "UPDATE PBFClinicPayee SET archived = true")
    @Modifying
	void archiveAll();

	@Query(value = "DELETE FROM PBFClinicPayee WHERE archived = true")
    @Modifying
	void deleteArchived();

	@Query(value = "UPDATE PBFClinicPayee SET archived = false WHERE archived = true")
    @Modifying
	void restoreArchived();
	
	@Query(value = "DELETE FROM PBFClinicPayee WHERE archived = false")
    @Modifying
	void deleteNew();
	
}
