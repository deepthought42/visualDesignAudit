package com.looksee.visualDesignAudit.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.looksee.visualDesignAudit.models.repository.DesignSystemRepository;
import com.looksee.visualDesignAudit.models.repository.DomainRepository;
import com.looksee.visualDesignAudit.models.AuditRecord;
import com.looksee.visualDesignAudit.models.DesignSystem;
import com.looksee.visualDesignAudit.models.Domain;
import com.looksee.visualDesignAudit.models.DomainAuditRecord;
import com.looksee.visualDesignAudit.models.Element;
import com.looksee.visualDesignAudit.models.Form;
import com.looksee.visualDesignAudit.models.PageState;
import com.looksee.visualDesignAudit.models.TestUser;

@Service
public class DomainService {
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DomainRepository domain_repo;
	
	@Autowired
	private DesignSystemRepository design_system_repo;

	public Set<Domain> getDomains() {
		return domain_repo.getDomains();
	}
	
	public Set<TestUser> getTestUsers(long domain_id) {
		return domain_repo.getTestUsers(domain_id);
	}

	public Domain findByHostForUser(String host, String username) {
		return domain_repo.findByHostForUser(host, username);
	}
	
	public Domain findByHost(String host) {
		return domain_repo.findByHost(host);
	}

	public Domain findByUrl(String url) {
		return domain_repo.findByUrl(url);
	}

	public int getTestCount(long account_id, String url) {
		return domain_repo.getTestCount(account_id, url);
	}

	public Optional<Domain> findById(long domain_id) {
		return domain_repo.findById(domain_id);
	}

	public Set<Form> getForms(long account_id, String url) {
		return domain_repo.getForms(account_id, url);
	}
	
	public int getFormCount(long account_id, String url) {
		return domain_repo.getFormCount(account_id, url);
	}

	public Set<Element> getElementStates(String url, String username) {
		return domain_repo.getElementStates(url, username);
	}

	public Set<PageState> getPageStates(long domain_id) {
		return domain_repo.getPageStates(domain_id);
	}

	public Domain findByKey(String key, String username) {
		return domain_repo.findByKey(key, username);
	}

	public Set<PageState> getPages(String domain_host) {
		return domain_repo.getPages(domain_host);
	}

	public Domain findByPageState(String page_state_key) {
		return domain_repo.findByPageState(page_state_key);
	}

	public Set<AuditRecord> getAuditRecords(String domain_key) {
		return domain_repo.getAuditRecords(domain_key);
	}

	public Domain findByAuditRecord(long audit_record_id) {
		return domain_repo.findByAuditRecord(audit_record_id);
	}

	public List<DomainAuditRecord> getAuditRecordHistory(long domain_id) {
		return domain_repo.getAuditRecordHistory(domain_id);
	}

	public Optional<DesignSystem> getDesignSystem(long domain_id) {
		return design_system_repo.getDesignSystemForDomain(domain_id);
	}

	public List<TestUser> findTestUsers(long domain_id) {
		return domain_repo.findTestUsers(domain_id);
	}
}
