package ca.bc.gov.hlth.pbfdataloader.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	@Autowired
    public JavaMailSender emailSender;
	
	/**
	 * Sends an email.
	 * @param recipients Semi-colon delimited list of recipients
	 * @param subject
	 * @param text
	 */
	public void sendNotificationEmail(String from, String recipients, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage(); 
		
		String[] to = StringUtils.split(recipients, ";");
		message.setFrom(from);
        message.setTo(to); 
        message.setSubject(subject); 
        message.setText(text);
        emailSender.send(message);
	}

}
