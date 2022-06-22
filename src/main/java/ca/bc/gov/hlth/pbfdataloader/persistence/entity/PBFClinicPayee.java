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
@Table(name = "pbf_clinic_payee")
public class PBFClinicPayee {

	@Id
	@Column(name = "pbf_clinic_payee_id", columnDefinition = "bigserial")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pbfClinicPayeeId;

	@Column(name = "payee_number")
	private String payeeNumber;

	@Column(name = "effective_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date effectiveDate;

	@Column(name = "cancel_date", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date cancelDate;

	@Column(name = "report_group", nullable = false)
	private String reportGroup;

	public Long getPbfClinicPayeeId() {
		return pbfClinicPayeeId;
	}

	public void setPbfClinicPayeeId(Long pbfClinicPayeeId) {
		this.pbfClinicPayeeId = pbfClinicPayeeId;
	}

	public String getPayeeNumber() {
		return payeeNumber;
	}

	public void setPayeeNumber(String payeeNumber) {
		this.payeeNumber = payeeNumber;
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

	public String getReportGroup() {
		return reportGroup;
	}

	public void setReportGroup(String reportGroup) {
		this.reportGroup = reportGroup;
	}

}
