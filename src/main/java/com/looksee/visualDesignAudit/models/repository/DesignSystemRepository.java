package com.looksee.visualDesignAudit.models.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.looksee.visualDesignAudit.models.DesignSystem;

import io.github.resilience4j.retry.annotation.Retry;

@Repository
@Retry(name = "neoforj")
public interface DesignSystemRepository extends Neo4jRepository<DesignSystem, Long> {
	
	@Query("MATCH (setting:DesignSystem) WHERE id(setting)=$id SET setting.audienceProficiency=$audience_proficiency RETURN setting")
	public DesignSystem updateExpertiseSetting(@Param("id") long domain_id, @Param("audience_proficiency") String audience_proficiency);

	@Query("MATCH (domain:Domain)-[:USES]->(ds:DesignSystem) WHERE id(domain)=$domain_id RETURN ds LIMIT 1")
	public Optional<DesignSystem> getDesignSystemForDomain(@Param("domain_id") long domain_id);
	
}
