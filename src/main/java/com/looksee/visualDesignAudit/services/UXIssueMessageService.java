package com.looksee.visualDesignAudit.services;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.looksee.visualDesignAudit.models.ColorContrastIssueMessage;
import com.looksee.visualDesignAudit.models.ElementState;
import com.looksee.visualDesignAudit.models.UXIssueMessage;
import com.looksee.visualDesignAudit.models.enums.AuditName;
import com.looksee.visualDesignAudit.models.repository.ColorContrastIssueMessageRepository;
import com.looksee.visualDesignAudit.models.repository.UXIssueMessageRepository;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.Synchronized;

@Service
@Retry(name="neoforj")
public class UXIssueMessageService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UXIssueMessageService.class);

	@Autowired
	private UXIssueMessageRepository issue_message_repo;
	
	@Autowired
	private ColorContrastIssueMessageRepository contrast_issue_message_repo;
	
	@Retryable
	public UXIssueMessage save(UXIssueMessage ux_issue) {
		return issue_message_repo.save(ux_issue);
	}
	
	@Retryable
	public ColorContrastIssueMessage saveColorContrast(ColorContrastIssueMessage ux_issue) {
		return contrast_issue_message_repo.save(ux_issue);
	}

	/**
	 * Find {@link UXIssueMessage} with a given key
	 * @param key used for identifying {@link UXIssueMessage}
	 * 
	 * @return updated {@link UXIssueMessage} object
	 * 
	 * @pre key != null
	 * @pre !key.isEmpty()
	 */
	public UXIssueMessage findByKey(String key) {
		assert key != null;
		assert !key.isEmpty();
		
		return issue_message_repo.findByKey(key);
	}

	public ElementState getElement(long id) {
		return issue_message_repo.getElement(id);
	}

	public Iterable<UXIssueMessage> saveAll(List<UXIssueMessage> issue_messages) {
		return issue_message_repo.saveAll(issue_messages);
		
	}

	public ElementState getGoodExample(long issue_id) {
		return issue_message_repo.getGoodExample(issue_id);
	}

	@Retryable
	@Synchronized
	public void addElement(long issue_id, long element_id) {
		issue_message_repo.addElement(issue_id, element_id);
	}

	public void addPage(long issue_id, long page_id) {
		issue_message_repo.addPage(issue_id, page_id);
	}

	public Set<UXIssueMessage> findByNameForElement(AuditName audit_name, long element_id) {
		return issue_message_repo.findByNameForElement(audit_name, element_id);
	}

	public boolean hasAuditBeenExecuted(AuditName audit_name, long element_id) {
		int count = issue_message_repo.getNumberOfUXIssuesForElement(audit_name, element_id);
		return count > 0;
	}
}
