package com.looksee.visualDesignAudit.models;

import java.util.Set;

import org.springframework.data.neo4j.core.schema.Node;

import com.looksee.visualDesignAudit.models.enums.AuditCategory;
import com.looksee.visualDesignAudit.models.enums.Priority;


/**
 * A observation of potential error for a given {@link Element element} 
 */
@Node
public class StockImageIssueMessage extends ElementStateIssueMessage {	
	private boolean stock_image;

	public StockImageIssueMessage() {}
	
	public StockImageIssueMessage(
			Priority priority,
			String description,
			String recommendation, 
			AuditCategory category, 
			Set<String> labels, 
			String wcag_compliance, 
			String title,
			int points_awarded, 
			int max_points,
			boolean is_stock_image
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
		
		setStockImage(is_stock_image);
	}

	public boolean isStockImage() {
		return stock_image;
	}

	public void setStockImage(boolean stock_image) {
		this.stock_image = stock_image;
	}
}
