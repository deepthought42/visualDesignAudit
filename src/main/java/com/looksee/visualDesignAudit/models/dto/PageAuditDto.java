package com.looksee.visualDesignAudit.models.dto;

import com.looksee.visualDesignAudit.models.enums.ExecutionStatus;

/**
 * Data transfer object for page audit progress that is designed to comply with
 * the data format for browser extensions
 */
public class PageAuditDto {
	private long id;
	private String url;
	private double content_score;
	private double content_progress;
	private double info_architecture_score;
	private double info_architecture_progress;
	private double accessibility_score;
	private double accessibility_progress;
	private double aesthetics_score;
	private double aesthetics_progress;
	private double data_extraction_progress;
	private String message;
	private String status;
	
	public PageAuditDto(){}

	public PageAuditDto(
			long id,
			String url,
			double content_score,
			double content_progress,
			double info_architecture_score,
			double info_architecture_progress,
			double accessibility_score,
			double aesthetics_score,
			double aesthetics_progress, 
			double data_extraction_progress,
			String message,
			ExecutionStatus status
	){
		setId(id);
		setUrl(url);
		setContentScore(content_score);
		setContentProgress(content_progress);
		setInfoArchitectureScore(info_architecture_score);
		setInfoArchitectureProgress(info_architecture_progress);
		setAccessibilityScore(accessibility_score);
		setAccessibilityProgress(accessibility_progress);
		setAestheticsScore(aesthetics_score);
		setAestheticsProgress(aesthetics_progress);
		setDataExtractionProgress(data_extraction_progress);
		setMessage(message);
		setStatus(status);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double getContentScore() {
		return content_score;
	}

	public void setContentScore(double content_score) {
		this.content_score = content_score;
	}

	public double getInfoArchitectureScore() {
		return info_architecture_score;
	}

	public void setInfoArchitectureScore(double info_architecture_score) {
		this.info_architecture_score = info_architecture_score;
	}

	public double getAccessibilityScore() {
		return accessibility_score;
	}

	public void setAccessibilityScore(double accessibility_score) {
		this.accessibility_score = accessibility_score;
	}

	public double getAestheticsScore() {
		return aesthetics_score;
	}

	public void setAestheticsScore(double aesthetics_score) {
		this.aesthetics_score = aesthetics_score;
	}

	public double getContentProgress() {
		return content_progress;
	}

	public void setContentProgress(double content_progress) {
		this.content_progress = content_progress;
	}

	public double getInfoArchitectureProgress() {
		return info_architecture_progress;
	}

	public void setInfoArchitectureProgress(double info_architecture_progress) {
		this.info_architecture_progress = info_architecture_progress;
	}

	public double getAccessibilityProgress() {
		return accessibility_progress;
	}

	public void setAccessibilityProgress(double accessibility_progress) {
		this.accessibility_progress = accessibility_progress;
	}

	public double getAestheticsProgress() {
		return aesthetics_progress;
	}

	public void setAestheticsProgress(double aesthetics_progress) {
		this.aesthetics_progress = aesthetics_progress;
	}

	public double getDataExtractionProgress() {
		return data_extraction_progress;
	}

	public void setDataExtractionProgress(double data_extraction_progress) {
		this.data_extraction_progress = data_extraction_progress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ExecutionStatus getStatus() {
		return ExecutionStatus.create(status);
	}

	public void setStatus(ExecutionStatus status) {
		this.status = status.getShortName();
	}	
}