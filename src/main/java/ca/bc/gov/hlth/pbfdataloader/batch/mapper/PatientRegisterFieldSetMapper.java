package ca.bc.gov.hlth.pbfdataloader.batch.mapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PatientRegister;

public class PatientRegisterFieldSetMapper implements FieldSetMapper<PatientRegister>{
	
	private static final String PBF_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public PatientRegister mapFieldSet(FieldSet fieldSet) throws BindException {
		
		PatientRegister patientRegister = new PatientRegister();
		patientRegister.setPhn(fieldSet.readString("PHN"));
		patientRegister.setPayeeNumber(fieldSet.readString("PAYENUM"));
		patientRegister.setRegisteredPractitionerNumber(fieldSet.readString("RPRCTNR"));
		patientRegister.setEffectiveDate(fieldSet.readDate("EFCTVDT", PBF_DATE_FORMAT));
		patientRegister.setCancelDate(fieldSet.readDate("EFCTVDT", PBF_DATE_FORMAT));
		patientRegister.setAdministrativeCode(StringUtils.equals(fieldSet.readString("SPCLND"), "1"));
		patientRegister.setRegistrationReasonCode(StringUtils.trimToNull(fieldSet.readString("RGRSNCD")));
		patientRegister.setDeregistrationReasonCode(StringUtils.trimToNull(fieldSet.readString("DRGRSNCD")));
//		patientRegister.setCancelReasonCode(fieldSet.readString("CNCLRSN"));

		return patientRegister;
	}

}
