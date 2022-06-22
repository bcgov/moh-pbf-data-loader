package ca.bc.gov.hlth.pbfdataloader.persistence.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "patient_register")
public class PatientRegister {

	@Id
	@Column(name = "patient_register_id", columnDefinition = "bigserial")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long patientRegisterId;

	@Column(name = "phn")
	private String phn;

	@Column(name = "payee_number")
	private String payeeNumber;

	@Column(name = "registered_practitioner_number", nullable = false)
	private String registeredPractitionerNumber;

	@Column(name = "effective_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date effectiveDate;

	@Column(name = "cancel_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date cancelDate;

	@Column(name = "special_indicator", nullable = false)
	private Boolean specialIndicator;

	@Column(name = "registration_reason_code", nullable = false)
	private String registrationReasonCode;

	@Column(name = "deregistration_reason_code", nullable = false)
	private String deregistrationReasonCode;

	@Column(name = "cancel_reason_code", nullable = false)
	private String cancelReasonCode;

	public Long getPatientRegisterId() {
		return patientRegisterId;
	}

	public void setPatientRegisterId(Long patientRegisterId) {
		this.patientRegisterId = patientRegisterId;
	}

	public String getPhn() {
		return phn;
	}

	public void setPhn(String phn) {
		this.phn = phn;
	}

	public String getPayeeNumber() {
		return payeeNumber;
	}

	public void setPayeeNumber(String payeeNumber) {
		this.payeeNumber = payeeNumber;
	}

	public String getRegisteredPractitionerNumber() {
		return registeredPractitionerNumber;
	}

	public void setRegisteredPractitionerNumber(String registeredPractitionerNumber) {
		this.registeredPractitionerNumber = registeredPractitionerNumber;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getCancelDate() {
		return cancelDate;
	}

	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
	}

	public Boolean getSpecialIndicator() {
		return specialIndicator;
	}

	public void setSpecialIndicator(Boolean specialIndicator) {
		this.specialIndicator = specialIndicator;
	}

	public String getRegistrationReasonCode() {
		return registrationReasonCode;
	}

	public void setRegistrationReasonCode(String registrationReasonCode) {
		this.registrationReasonCode = registrationReasonCode;
	}

	public String getDeregistrationReasonCode() {
		return deregistrationReasonCode;
	}

	public void setDeregistrationReasonCode(String deregistrationReasonCode) {
		this.deregistrationReasonCode = deregistrationReasonCode;
	}

	public String getCancelReasonCode() {
		return cancelReasonCode;
	}

	public void setCancelReasonCode(String cancelReasonCode) {
		this.cancelReasonCode = cancelReasonCode;
	}

}
