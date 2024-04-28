package com.looksee.visualDesignAudit.models.message;

import com.looksee.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.visualDesignAudit.models.enums.AuditLevel;

import lombok.Getter;
import lombok.Setter;

/**
 * Intended to contain information about progress an audit
 */
public class AuditProgressUpdate extends Message {
	@Getter
	@Setter
	private long pageAuditId;
	
	@Getter
	@Setter
	private AuditCategory category;
	
	@Getter
	@Setter
	private AuditLevel level;
	
	@Getter
	@Setter
	private double progress;
	
	@Getter
	@Setter
	private String message;
	
	public AuditProgressUpdate() {	}
	
	public AuditProgressUpdate(
			long account_id,
			double progress,
			String message,
			AuditCategory category,
			AuditLevel level,
			long page_audit_id
	) {
		super(account_id);
		setProgress(progress);
		setMessage(message);
		setCategory(category);
		setLevel(level);
		setPageAuditId(page_audit_id);
	}
}
