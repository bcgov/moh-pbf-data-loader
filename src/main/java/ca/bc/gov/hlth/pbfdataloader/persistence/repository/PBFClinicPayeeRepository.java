package ca.bc.gov.hlth.pbfdataloader.persistence.repository;

import java.util.List;

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
	
	List<PBFClinicPayee> findByArchived(Boolean archived);
	
}
