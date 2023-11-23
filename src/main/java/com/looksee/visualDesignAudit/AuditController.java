package com.looksee.visualDesignAudit;

/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// [START cloudrun_pubsub_handler]
// [START run_pubsub_handler]
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.looksee.visualDesignAudit.models.dto.PageAuditDto;
import com.looksee.visualDesignAudit.models.enums.ExecutionStatus;
import com.looksee.utils.AuditUtils;
import com.looksee.visualDesignAudit.audit.NonTextColorContrastAudit;
import com.looksee.visualDesignAudit.audit.TextColorContrastAudit;
import com.looksee.visualDesignAudit.gcp.PubSubAuditUpdatePublisherImpl;
import com.looksee.visualDesignAudit.mapper.Body;
import com.looksee.visualDesignAudit.models.Audit;
import com.looksee.visualDesignAudit.models.AuditRecord;
import com.looksee.visualDesignAudit.models.DesignSystem;
import com.looksee.visualDesignAudit.models.Domain;
import com.looksee.visualDesignAudit.models.ElementState;
import com.looksee.visualDesignAudit.models.PageState;
import com.looksee.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.visualDesignAudit.models.enums.AuditLevel;
import com.looksee.visualDesignAudit.models.enums.AuditName;
import com.looksee.visualDesignAudit.models.message.AuditProgressUpdate;
import com.looksee.visualDesignAudit.models.message.PageAuditMessage;
import com.looksee.visualDesignAudit.services.AuditRecordService;
import com.looksee.visualDesignAudit.services.DomainService;
import com.looksee.visualDesignAudit.services.MessageBroadcaster;
import com.looksee.visualDesignAudit.services.PageStateService;

// PubsubController consumes a Pub/Sub message.
@RestController
public class AuditController {
	private static Logger log = LoggerFactory.getLogger(AuditController.class);

	@Autowired
	private AuditRecordService audit_record_service;
	
	@Autowired
	private DomainService domain_service;
	
	@Autowired
	private PageStateService page_state_service;
	
	@Autowired
	private TextColorContrastAudit text_contrast_audit_impl;
	
	@Autowired
	private NonTextColorContrastAudit non_text_contrast_audit_impl;
	
	@Autowired
	private PubSubAuditUpdatePublisherImpl audit_update_topic;
	
	@Autowired
	private MessageBroadcaster pusher;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> receiveMessage(@RequestBody Body body) 
			throws JsonMappingException, JsonProcessingException, ExecutionException, InterruptedException 
	{
		Body.Message message = body.getMessage();
		String data = message.getData();
	    String target = !data.isEmpty() ? new String(Base64.getDecoder().decode(data)) : "";
        log.warn("page audit msg received = "+target);

	    ObjectMapper mapper = new ObjectMapper();
	    PageAuditMessage audit_record_msg = mapper.readValue(target, PageAuditMessage.class);
    
    	Domain domain = domain_service.findByAuditRecord(audit_record_msg.getPageAuditId());
		DesignSystem design_system = domain_service.getDesignSystem(domain.getId()).get();

		AuditRecord audit_record = audit_record_service.findById(audit_record_msg.getPageAuditId()).get();
		PageState page = page_state_service.getPageStateForAuditRecord(audit_record.getId());
		List<ElementState> elements = page_state_service.getElementStates(page.getId());
		page.setElements(elements);

    	Set<Audit> audits = audit_record_service.getAllAudits(audit_record.getId());

		if(!auditAlreadyExists(audits, AuditName.TEXT_BACKGROUND_CONTRAST)) {
		   	Audit text_contrast_audit = text_contrast_audit_impl.execute(page, audit_record, design_system);
		   	audit_record_service.addAudit(audit_record_msg.getPageAuditId(), text_contrast_audit);
		   	audits.add(text_contrast_audit);
		}
	
						
		if(!auditAlreadyExists(audits, AuditName.NON_TEXT_BACKGROUND_CONTRAST)) {    			
			Audit non_text_contrast_audit = non_text_contrast_audit_impl.execute(page, audit_record, design_system);
			audit_record_service.addAudit(audit_record_msg.getPageAuditId(), non_text_contrast_audit);
		   	audits.add(non_text_contrast_audit);
		}
    		
	    		
	    AuditProgressUpdate audit_update = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																	audit_record_msg.getPageAuditId(),
																	1.0, 
																	"Content Audit Compelete!",
																	AuditCategory.CONTENT, 
																	AuditLevel.PAGE);

		String audit_record_json = mapper.writeValueAsString(audit_update);
		
		audit_update_topic.publish(audit_record_json);

		PageAuditDto audit_dto = builPagedAuditdDto(audit_record_msg.getPageAuditId(), page.getUrl());
		pusher.sendAuditUpdate(Long.toString( audit_record_msg.getAccountId() ), audit_dto);
		return new ResponseEntity<String>("Successfully completed visual design audit", HttpStatus.OK);
	}
	
	/**
	 * Creates an {@linkplain PageAuditDto} using page audit ID and the provided page_url
	 * @param pageAuditId
	 * @param page_url
	 * @return
	 */
	private PageAuditDto builPagedAuditdDto(long pageAuditId, String page_url) {
		//get all audits
		Set<Audit> audits = audit_record_service.getAllAudits(pageAuditId);
		Set<AuditName> audit_labels = new HashSet<AuditName>();
		audit_labels.add(AuditName.TEXT_BACKGROUND_CONTRAST);
		audit_labels.add(AuditName.NON_TEXT_BACKGROUND_CONTRAST);
		audit_labels.add(AuditName.TITLES);
		audit_labels.add(AuditName.IMAGE_COPYRIGHT);
		audit_labels.add(AuditName.IMAGE_POLICY);
		audit_labels.add(AuditName.LINKS);
		audit_labels.add(AuditName.ALT_TEXT);
		audit_labels.add(AuditName.METADATA);
		audit_labels.add(AuditName.READING_COMPLEXITY);
		audit_labels.add(AuditName.PARAGRAPHING);
		audit_labels.add(AuditName.ENCRYPTED);
		//count audits for each category
		//calculate content score
		//calculate aesthetics score
		//calculate information architecture score
		double visual_design_progress = AuditUtils.calculateProgress(AuditCategory.AESTHETICS, 
																 1, 
																 audits, 
																 AuditUtils.getAuditLabels(AuditCategory.AESTHETICS, audit_labels));
		double content_progress = AuditUtils.calculateProgress(AuditCategory.CONTENT, 
																1, 
																audits, 
																audit_labels);
		double info_architecture_progress = AuditUtils.calculateProgress(AuditCategory.INFORMATION_ARCHITECTURE, 
																		1, 
																		audits, 
																		audit_labels);

		double content_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.CONTENT);
		double info_architecture_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.INFORMATION_ARCHITECTURE);
		double visual_design_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.AESTHETICS);
		double a11y_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.ACCESSIBILITY);

		double data_extraction_progress = 1;
		String message = "";
		ExecutionStatus execution_status = ExecutionStatus.UNKNOWN;
		if(visual_design_progress < 1 || content_progress < 1 || visual_design_progress < 1) {
			execution_status = ExecutionStatus.IN_PROGRESS;
		}
		else {
			execution_status = ExecutionStatus.COMPLETE;
		}
		
		return new PageAuditDto(pageAuditId, 
								page_url, 
								content_score, 
								content_progress, 
								info_architecture_score, 
								info_architecture_progress, 
								a11y_score,
								visual_design_score,
								visual_design_progress,
								data_extraction_progress, 
								message, 
								execution_status);
	}
	
	/**
	 * Checks if the any of the provided {@link Audit audits} have a name that matches 
	 * 		the provided {@linkplain AuditName}
	 * 
	 * @param audits
	 * @param audit_name
	 * 
	 * @return
	 * 
	 * @pre audits != null
	 * @pre audit_name != null
	 */
	private boolean auditAlreadyExists(Set<Audit> audits, AuditName audit_name) {
		assert audits != null;
		assert audit_name != null;
		
		for(Audit audit : audits) {
			if(audit_name.equals(audit.getName())) {
				return true;
			}
		}
		return false;
	}
}
// [END run_pubsub_handler]
// [END cloudrun_pubsub_handler]