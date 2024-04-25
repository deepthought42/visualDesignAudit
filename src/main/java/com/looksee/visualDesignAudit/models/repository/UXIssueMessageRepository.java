package com.looksee.visualDesignAudit.models.repository;

import java.util.Set;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.looksee.visualDesignAudit.models.ElementState;
import com.looksee.visualDesignAudit.models.UXIssueMessage;
import com.looksee.visualDesignAudit.models.enums.AuditName;

import io.github.resilience4j.retry.annotation.Retry;

@Repository
@Retry(name="neoforj")
public interface UXIssueMessageRepository extends Neo4jRepository<UXIssueMessage, Long>  {
	public UXIssueMessage findByKey(@Param("key") String key);

	@Query("MATCH (uim:UXIssueMessage)-[:FOR]->(e:ElementState) WHERE id(uim)=$id RETURN e")
	public ElementState getElement(@Param("id") long id);

	@Query("MATCH (uim:UXIssueMessage)-[:EXAMPLE]->(e:ElementState) WHERE id(uim)=$id RETURN e")
	public ElementState getGoodExample(@Param("id") long issue_id);

	@Query("MATCH (uim:UXIssueMessage) WITH uim MATCH (e:ElementState) WHERE id(uim)=$issue_id AND id(e)=$element_id MERGE (uim)-[r:FOR]->(e) RETURN r")
	public void addElement(@Param("issue_id") long issue_id, @Param("element_id") long element_id);

	@Query("MATCH (uim:UXIssueMessage) WITH uim MATCH (e:PageState) WHERE id(uim)=$issue_id AND id(e)=$page_id MERGE (uim)-[r:FOR]->(e) RETURN r")
	public void addPage(@Param("issue_id") long issue_id, @Param("page_id") long page_id);

	@Query("MATCH w=(a:Audit)-[]->(ux:UXIssueMessage) MATCH y=(ux:UXIssueMessage)-[:FOR]->(e:ElementState) WHERE id(e)=$element_id AND a.name=$name RETURN ux")
	public Set<UXIssueMessage> findByNameForElement(@Param("name") AuditName name, @Param("element_id") long element_id);

	@Query("MATCH w=(a:Audit)-[]->(ux:UXIssueMessage) MATCH y=(ux:UXIssueMessage)-[:FOR]->(e:ElementState) WHERE id(e)=$element_id AND a.name=$name RETURN COUNT(ux)")
	public int getNumberOfUXIssuesForElement(@Param("name") AuditName audit_name, @Param("element_id") long element_id);
}
