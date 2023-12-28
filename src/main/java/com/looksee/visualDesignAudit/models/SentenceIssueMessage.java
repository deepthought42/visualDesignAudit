package com.looksee.visualDesignAudit.models;

import java.util.Set;

import org.springframework.data.neo4j.core.schema.Node;

import com.looksee.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.visualDesignAudit.models.enums.Priority;


/**
 * A observation of potential error for a given {@link Element element} 
 */
@Node
public class SentenceIssueMessage extends ElementStateIssueMessage {	
	
	private int word_count;
	
	public SentenceIssueMessage() {}
	
	public SentenceIssueMessage(
			Priority priority,
			String description,
			String recommendation, 
			AuditCategory category, 
			Set<String> labels, 
			String wcag_compliance, 
			String title,
			int points_awarded, 
			int max_points,
			int word_count
	) {
		super(	priority, 
				description,
				recommendation,
				category,
				labels,
				wcag_compliance,
				title,
				points_awarded,
				max_points, null);
		
		setWordCount(word_count);
	}

	public int getWordCount() {
		return word_count;
	}

	public void setWordCount(int word_count) {
		this.word_count = word_count;
	}

}
