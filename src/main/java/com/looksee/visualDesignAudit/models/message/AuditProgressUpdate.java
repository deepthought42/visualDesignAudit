package com.looksee.visualDesignAudit.models.message;

import com.looksee.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.visualDesignAudit.models.enums.AuditLevel;

/**
 * Intended to contain information about progress an audit
 */
public class AuditProgressUpdate extends PageAuditMessage {
	private long pageAuditId;
	private AuditCategory category;
	private AuditLevel level;
	private double progress;
	private String message;
	
	public AuditProgressUpdate() {	}
	
	public AuditProgressUpdate(
			long account_id,
			long page_audit_id,
			double progress,
			String message, 
			AuditCategory category,
			AuditLevel level
	) {
		super(account_id, page_audit_id);
		setProgress(progress);
		setMessage(message);
		setCategory(category);
		setLevel(level);
	}
	
	/* GETTERS / SETTERS */
	public double getProgress() {
		return progress;
	}
	public void setProgress(double progress) {
		this.progress = progress;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public AuditCategory getCategory() {
		return category;
	}

	public void setCategory(AuditCategory audit_category) {
		this.category = audit_category;
	}

	public AuditLevel getLevel() {
		return level;
	}

	public void setLevel(AuditLevel level) {
		this.level = level;
	}

	public long getPageAuditId() {
		return pageAuditId;
	}

	public void setPageAuditId(long page_audit_id) {
		this.pageAuditId = page_audit_id;
	}
}
