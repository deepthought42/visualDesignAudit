package com.looksee.visualDesignAudit.gcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PubSubAuditRecordPublisherImpl extends PubSubPublisher {

    @SuppressWarnings("unused")
	private static Logger LOG = LoggerFactory.getLogger(PubSubAuditRecordPublisherImpl.class);

    @Value("${pubsub.page_audit_topic}")
    private String topic;
    
    @Override
    protected String topic() {
        return this.topic;
    }
}