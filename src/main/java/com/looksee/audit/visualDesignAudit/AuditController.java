package com.looksee.audit.visualDesignAudit;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.looksee.audit.visualDesignAudit.mapper.Body;
import com.looksee.audit.visualDesignAudit.models.Audit;
import com.looksee.audit.visualDesignAudit.models.AuditRecord;
import com.looksee.audit.visualDesignAudit.models.DesignSystem;
import com.looksee.audit.visualDesignAudit.models.PageAuditRecord;
import com.looksee.audit.visualDesignAudit.models.PageState;
import com.looksee.audit.visualDesignAudit.models.dto.PageBuiltMessage;
import com.looksee.audit.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.audit.visualDesignAudit.models.enums.AuditLevel;
import com.looksee.audit.visualDesignAudit.models.enums.ExecutionStatus;
import com.looksee.audit.visualDesignAudit.services.AuditRecordService;
import com.looksee.audit.visualDesignAudit.services.DomainService;
import com.looksee.audit.visualDesignAudit.services.PageStateService;
import com.looksee.audit.visualDesignAudit.gcp.PubSubErrorPublisherImpl;
import com.looksee.audit.visualDesignAudit.gcp.PubSubPublisherImpl;

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
	private PubSubPublisherImpl pubSubPublisherImpl;
	
	@Autowired
	private PubSubErrorPublisherImpl pubSubErrorPublisherImpl;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity receiveMessage(@RequestBody Body body) throws JsonMappingException, JsonProcessingException, ExecutionException, InterruptedException {
		Body.Message message = body.getMessage();
	    log.warn("message " + message);
	    
	    String data = message.getData();
	    log.warn("data :: "+data);
	    //retrieve audit record and determine type of audit record
	    
	    byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
	    String decoded_json = new String(decodedBytes);

	    //create ObjectMapper instance
	    ObjectMapper objectMapper = new ObjectMapper();
	    
	    //convert json string to object
	    PageBuiltMessage audit_record_msg = objectMapper.readValue(decoded_json, PageBuiltMessage.class);
	    //retrieve compliance level
	    DesignSystem design_system = null;

	    if(page_audit_record_msg.getDomainId() >= 0 ) {
			
			Optional<DesignSystem> design_system_opt = domain_service.getDesignSystem(page_audit_record_msg.getDomainId());
			if(!design_system_opt.isPresent()) {
				log.warn("design system couldn't be found for domain :: "+page_audit_record_msg.getDomainId());
				design_system = design_system_service.save( new DesignSystem() );
				domain_service.addDesignSystem(page_audit_record_msg.getDomainId(), design_system.getId());
			}
			else {
				design_system = design_system_opt.get();
			}
		}
		AuditRecord audit_record = audit_record_service.findById(page_audit_record_msg.getPageAuditId()).get();
		PageState page = page_state_service.findById(audit_record.getId());
	   	//PageState page = page_audit_record_msg.getPageState();
	   	//check if page state already
			//perform audit and return audit result
	   
	   	//Audit color_palette_audit = color_palette_auditor.execute(page);
		//audits.add(color_palette_audit);
	   	AuditProgressUpdate audit_update = new AuditProgressUpdate(
													page_audit_record_msg.getAccountId(),
													audit_record.getId(),
													(1.0/3.0),
													"Reviewing text contrast",
													AuditCategory.AESTHETICS,
													AuditLevel.PAGE, 
													null, 
													page_audit_record_msg.getDomainId());

	   	getSender().tell(audit_update, getSelf());

		/*
		Audit padding_audits = padding_auditor.execute(page);
		audits.add(padding_audits);

		Audit margin_audits = margin_auditor.execute(page);
		audits.add(margin_audits);
		 */
		
		try {
		   	Audit text_contrast_audit = text_contrast_auditor.execute(page, audit_record, design_system);
		   	if(text_contrast_audit != null) {
				AuditProgressUpdate audit_update2 = new AuditProgressUpdate(
															page_audit_record_msg.getAccountId(),
															audit_record.getId(),
															(2.0/3.0),
															"Reviewing non-text contrast for WCAG compliance",
															AuditCategory.AESTHETICS,
															AuditLevel.PAGE, 
															text_contrast_audit, 
															page_audit_record_msg.getDomainId());
				getSender().tell(audit_update2, getSelf());
		   	}
		}
		catch(Exception e) {
			AuditError audit_err = new AuditError(page_audit_record_msg.getDomainId(), 
												  page_audit_record_msg.getAccountId(), 
												  page_audit_record_msg.getAuditRecordId(), 
												  "An error occurred while performing non-text audit", 
												  AuditCategory.AESTHETICS, 
												  (2.0/3.0));
			getSender().tell(audit_err, getSelf());
			e.printStackTrace();
		}
		
		
		try {
			Audit non_text_contrast_audit = non_text_contrast_auditor.execute(page, audit_record, design_system);
			if( non_text_contrast_audit != null ) {
			
				AuditProgressUpdate audit_update3 = new AuditProgressUpdate(
															page_audit_record_msg.getAccountId(),
															audit_record.getId(),
															1.0,
															"Completed review of non-text contrast",
															AuditCategory.AESTHETICS,
															AuditLevel.PAGE, 
															non_text_contrast_audit, 
															page_audit_record_msg.getDomainId());

				getSender().tell(audit_update3, getSelf());
			}
		}
		catch(Exception e) {
			AuditError audit_err = new AuditError(page_audit_record_msg.getDomainId(), 
												  page_audit_record_msg.getAccountId(), 
												  page_audit_record_msg.getAuditRecordId(), 
												  "An error occurred while performing non-text audit", 
												  AuditCategory.AESTHETICS, 
												  1.0);
			getSender().tell(audit_err, getSelf());
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

		
		
		AuditProgressUpdate audit_update3 = new AuditProgressUpdate(
				page_audit_record_msg.getAccountId(),
				page_audit_record_msg.getPageAuditId(),
				1.0,
				"Completed review of non-text contrast",
				AuditCategory.AESTHETICS,
				AuditLevel.PAGE, 
				null,
				page_audit_record_msg.getDomainId());

		getContext().getParent().tell(audit_update3, getSelf());
	}
	
	return new ResponseEntity("Successfully sent message to audit manager", HttpStatus.OK);
    
  }
  /*
  public void publishMessage(String messageId, Map<String, String> attributeMap, String message) throws ExecutionException, InterruptedException {
      log.info("Sending Message to the topic:::");
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
              .putAllAttributes(attributeMap)
              .setData(ByteString.copyFromUtf8(message))
              .setMessageId(messageId)
              .build();

      pubSubPublisherImpl.publish(pubsubMessage);
  }
  */
}
// [END run_pubsub_handler]
// [END cloudrun_pubsub_handler]