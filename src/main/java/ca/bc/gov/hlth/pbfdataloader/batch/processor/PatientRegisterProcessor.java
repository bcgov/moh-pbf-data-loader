package ca.bc.gov.hlth.pbfdataloader.batch.processor;

import org.springframework.batch.item.ItemProcessor;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;

public class PatientRegisterProcessor implements ItemProcessor<PatientRegister, PatientRegister> {

	@Override
	public PatientRegister process(PatientRegister patientRegister) throws Exception {
		// No additional processing is required
		return patientRegister;
	}

}
