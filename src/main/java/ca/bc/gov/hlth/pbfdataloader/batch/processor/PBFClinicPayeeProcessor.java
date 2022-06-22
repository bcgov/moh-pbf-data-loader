package ca.bc.gov.hlth.pbfdataloader.batch.processor;

import org.springframework.batch.item.ItemProcessor;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;

public class PBFClinicPayeeProcessor implements ItemProcessor<PBFClinicPayee, PBFClinicPayee> {

	@Override
	public PBFClinicPayee process(PBFClinicPayee payee) throws Exception {
		// No additional processing is required
		return payee;
	}

}
