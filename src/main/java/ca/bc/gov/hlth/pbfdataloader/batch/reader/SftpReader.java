package ca.bc.gov.hlth.pbfdataloader.batch.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import ca.bc.gov.hlth.pbfdataloader.persistence.entity.PBFClinicPayee;

public class SftpReader implements ItemReader<PBFClinicPayee> {

	@Override
	public PBFClinicPayee read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		// TODO Auto-generated method stub
		return null;
	}

}
