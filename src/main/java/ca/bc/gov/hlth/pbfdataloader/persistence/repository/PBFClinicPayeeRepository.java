package ca.bc.gov.hlth.pbfdataloader.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;

@Repository
public interface PBFClinicPayeeRepository extends CrudRepository<PBFClinicPayee, Long>{

}
