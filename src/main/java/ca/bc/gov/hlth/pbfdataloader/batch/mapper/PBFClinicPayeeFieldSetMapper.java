package ca.bc.gov.hlth.pbfdataloader.batch.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;

public class PBFClinicPayeeFieldSetMapper implements FieldSetMapper<PBFClinicPayee>{
	
	private static final String PBF_DATE_FORMAT = "yyyy-MM-dd";

	@Override
	public PBFClinicPayee mapFieldSet(FieldSet fieldSet) throws BindException {
		
		PBFClinicPayee payee = new PBFClinicPayee();
		payee.setPayeeNumber(fieldSet.readString("PAYENUM"));
		payee.setEffectiveDate(fieldSet.readDate("EFCTVDT", PBF_DATE_FORMAT));
		payee.setCancelDate(fieldSet.readDate("CNCLDT", PBF_DATE_FORMAT));
		payee.setReportGroup(fieldSet.readString("RPTGRP"));

		return payee;
	}

}
