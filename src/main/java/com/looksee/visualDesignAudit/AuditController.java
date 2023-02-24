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
import com.looksee.visualDesignAudit.audit.TextColorContrastAudit;
import com.looksee.visualDesignAudit.mapper.Body;
import com.looksee.visualDesignAudit.models.Audit;
import com.looksee.visualDesignAudit.models.AuditRecord;
import com.looksee.visualDesignAudit.models.DesignSystem;
import com.looksee.visualDesignAudit.models.PageState;
import com.looksee.visualDesignAudit.models.message.PageAuditMessage;
import com.looksee.visualDesignAudit.services.AuditRecordService;
import com.looksee.visualDesignAudit.services.DomainService;
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
	private TextColorContrastAudit non_text_contrast_audit_impl;
	
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> receiveMessage(@RequestBody Body body) 
			throws JsonMappingException, JsonProcessingException, ExecutionException, InterruptedException 
	{
		Body.Message message = body.getMessage();
		String data = message.getData();
	    String target = !data.isEmpty() ? new String(Base64.getDecoder().decode(data)) : "";
        log.warn("page audit msg received = "+target);

	    ObjectMapper input_mapper = new ObjectMapper();
	    PageAuditMessage audit_record_msg = input_mapper.readValue(target, PageAuditMessage.class);
	    
	    //JsonMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

	    try {
		    //retrieve compliance level
	    	/**
		    DesignSystem design_system = null;
	
			TODO: Move this code block to be performed when a new domain audit is started
			
		    String data_str = body.getMessage().getData();
		    if(audit_record_msg.getDomainAuditRecordId() >= 0 ) {
				
				Optional<DesignSystem> design_system_opt = domain_service.getDesignSystem(audit_record_msg.getDomainId());
				if(!design_system_opt.isPresent()) {
					log.warn("design system couldn't be found for domain :: " + audit_record_msg.getDomain());
					design_system = design_system_service.save( new DesignSystem() );
					domain_service.addDesignSystem(audit_record_msg.getDomainId(), design_system.getId());
				}
				else {
					design_system = design_system_opt.get();
				}
			}
				     */
		    
	    	log.warn("retrieving design system  for domain with id: " +audit_record_msg.getDomainId());
			DesignSystem design_system = domain_service.getDesignSystem(audit_record_msg.getDomainId()).get();

			log.warn("Retreiving page audit record with id = "+audit_record_msg.getPageAuditId());
			AuditRecord audit_record = audit_record_service.findById(audit_record_msg.getPageAuditId()).get();
			
			log.warn("Looking up PageState with id = "+audit_record_msg.getPageId());
			PageState page = page_state_service.findById(audit_record_msg.getPageId()).get();
		   	//check if page state already
				//perform audit and return audit result
		   
		   	//Audit color_palette_audit = color_palette_auditor.execute(page);
			//audits.add(color_palette_audit);
			/*
			log.warn("creating initial audit progress update");
		   	AuditProgressUpdate audit_update = new AuditProgressUpdate(
		   												audit_record_msg.getAccountId(),
														audit_record_msg.getDomainAuditRecordId(),
														0.05,
														"Reviewing text contrast",
														AuditCategory.AESTHETICS,
														AuditLevel.PAGE,
														audit_record_msg.getDomainId(), 
														audit_record_msg.getPageAuditId());	
		   	
		    String audit_record_json = mapper.writeValueAsString(audit_update);
			log.warn("audit progress update = "+audit_record_json);
			//TODO: SEND PUB SUB MESSAGE THAT AUDIT RECORD NOT FOUND WITH PAGE DATA EXTRACTION MESSAGE
			audit_update_topic.publish(audit_record_json);
		   	*/
			/*
			Audit padding_audits = padding_auditor.execute(page);
			audits.add(padding_audits);
	
			Audit margin_audits = margin_auditor.execute(page);
			audits.add(margin_audits);
			 */
			
			try {
			   	Audit text_contrast_audit = text_contrast_audit_impl.execute(page, audit_record, design_system);
			   	audit_record_service.addAudit(audit_record_msg.getPageAuditId(), text_contrast_audit.getId());
			   	/* 
			   	if(text_contrast_audit != null) {
	
					AuditProgressUpdate audit_update2 = new AuditProgressUpdate(
																audit_record_msg.getAccountId(),
																audit_record_msg.getDomainAuditRecordId(),
																(2.0/3.0),
																"Reviewing non-text contrast for WCAG compliance",
																AuditCategory.AESTHETICS,
																AuditLevel.PAGE,
																audit_record_msg.getDomainId(), 
																audit_record_msg.getPageAuditId());
					
					audit_record_json = mapper.writeValueAsString(audit_update2);
				
					log.warn("audit progress update = "+audit_record_json);
					//TODO: SEND PUB SUB MESSAGE THAT AUDIT RECORD NOT FOUND WITH PAGE DATA EXTRACTION MESSAGE
					audit_record_topic.publish(audit_record_json);
			   	}
			   	 */
			}
			catch(Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while performing non-text audit", 
													  AuditCategory.AESTHETICS, 
													  (2.0/3.0),
													  audit_record_msg.getDomainId());
				
				audit_record_json = mapper.writeValueAsString(audit_err);
				log.warn("audit progress update = "+audit_record_json);

				//TODO: SEND PUB SUB MESSAGE THAT AUDIT RECORD NOT FOUND WITH PAGE DATA EXTRACTION MESSAGE
				pubSubErrorPublisherImpl.publish(audit_record_json);
				*/
				e.printStackTrace();
			}
			
			
			try {
				Audit non_text_contrast_audit = non_text_contrast_audit_impl.execute(page, audit_record, design_system);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), non_text_contrast_audit.getId());
				/*
				if( non_text_contrast_audit != null ) {
				
					AuditProgressUpdate audit_update3 = new AuditProgressUpdate(
																audit_record_msg.getAccountId(), 
																audit_record_msg.getDomainAuditRecordId(),
																1.0,
																"Completed review of non-text contrast",
																AuditCategory.AESTHETICS,
																AuditLevel.PAGE, 
																audit_record_msg.getDomainId(), 
																audit_record_msg.getPageAuditId());
					
					
					audit_record_json = mapper.writeValueAsString(audit_update3);
					log.warn("audit progress update = "+audit_record_json);
					//TODO: SEND PUB SUB MESSAGE THAT AUDIT RECORD NOT FOUND WITH PAGE DATA EXTRACTION MESSAGE
					audit_record_topic.publish(audit_record_json);
				}
				 */
			}
			catch(Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while performing non-text audit", 
													  AuditCategory.AESTHETICS, 
													  1.0,
													  audit_record_msg.getDomainId());
				
				audit_record_json = mapper.writeValueAsString(audit_err);
				log.warn("audit progress update = "+audit_record_json);

				//TODO: SEND PUB SUB MESSAGE THAT AUDIT RECORD NOT FOUND WITH PAGE DATA EXTRACTION MESSAGE
				pubSubErrorPublisherImpl.publish(audit_record_json);
			    */
				e.printStackTrace();
			}
			
		}catch(Exception e) {
			log.warn("exception caught during aesthetic audit");
			e.printStackTrace();
			log.warn("-------------------------------------------------------------");
			log.warn("-------------------------------------------------------------");
			log.warn("THERE WAS AN ISSUE DURING AESTHETICS AUDIT");
			log.warn("-------------------------------------------------------------");
			log.warn("-------------------------------------------------------------");
	
			
			/*
			AuditProgressUpdate audit_update3 = new AuditProgressUpdate(
														audit_record_msg.getAccountId(),
														audit_record_msg.getDomainAuditRecordId(),
														1.0,
														"Completed review of non-text contrast",
														AuditCategory.AESTHETICS,
														AuditLevel.PAGE, 
														audit_record_msg.getDomainId(), 
														audit_record_msg.getPageAuditId());
	
		    String audit_record_json = mapper.writeValueAsString(audit_update3);
			log.warn("audit progress update = "+audit_record_json);

			//TODO: SEND PUB SUB MESSAGE THAT AUDIT RECORD NOT FOUND WITH PAGE DATA EXTRACTION MESSAGE
		    pubSubErrorPublisherImpl.publish(audit_record_json);
		    */
		}

	    /*
	    AuditProgressUpdate audit_update = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																	audit_record_msg.getDomainAuditRecordId(),
																	1.0, 
																	"Content Audit Compelete!",
																	AuditCategory.CONTENT, 
																	AuditLevel.PAGE, 
																	audit_record_msg.getDomainId(), 
																	audit_record_msg.getPageAuditId());

		String audit_record_json = mapper.writeValueAsString(audit_update);
		
		audit_update_topic.publish(audit_record_json);
		*/
	    
		return new ResponseEntity<String>("Successfully completed visual design audit", HttpStatus.OK);
	}
}
// [END run_pubsub_handler]
// [END cloudrun_pubsub_handler]