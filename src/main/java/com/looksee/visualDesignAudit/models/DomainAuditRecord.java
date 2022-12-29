package com.looksee.visualDesignAudit.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Relationship;

import com.looksee.visualDesignAudit.models.enums.AuditLevel;
import com.looksee.visualDesignAudit.models.enums.ExecutionStatus;


/**
 * Record detailing an set of {@link Audit audits}.
 */
public class DomainAuditRecord extends AuditRecord {
	private int total_pages;
	
	@Relationship(type = "HAS")
	private Set<PageAuditRecord> page_audit_records;
	
	public DomainAuditRecord() {
		super();
		setAudits(new HashSet<>());
	}
	
	/**
	 * Constructor
	 * 
	 * @param audit_stats {@link AuditStats} object with statics for audit progress
	 * @param level TODO
	 * 
	 * @pre audit_stats != null;
	 */
	public DomainAuditRecord(ExecutionStatus status) {
		super();
		assert status != null;
		
		setAudits(new HashSet<>());
		setStatus(status);
		setLevel( AuditLevel.DOMAIN);
		setStartTime(LocalDateTime.now());
		setAestheticAuditProgress(0.0);
		setContentAuditProgress(0.0);
		setInfoArchitectureAuditProgress(0.0);
		setDataExtractionProgress(0.0);
		setTotalPages(0);
		setKey(generateKey());
	}

	public String generateKey() {
		return "domainauditrecord:"+UUID.randomUUID().toString()+org.apache.commons.codec.digest.DigestUtils.sha256Hex(System.currentTimeMillis() + "");
	}

	public Set<PageAuditRecord> getAudits() {
		return page_audit_records;
	}

	public void setAudits(Set<PageAuditRecord> audits) {
		this.page_audit_records = audits;
	}

	public void addAudit(PageAuditRecord audit) {
		this.page_audit_records.add( audit );
	}
	
	public void addAudits(Set<PageAuditRecord> audits) {
		this.page_audit_records.addAll( audits );
	}

	public int getTotalPages() {
		return total_pages;
	}

	public void setTotalPages(int total_pages) {
		this.total_pages = total_pages;
	}
}
