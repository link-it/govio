package it.govhub.govio.api.repository;

import java.time.OffsetDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioMessageEntity_;
import it.govhub.govio.api.entity.GovioServiceEntity_;
import it.govhub.govio.api.entity.ServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;

public class GovioMessageFilters {

	
	  public static Specification<GovioMessageEntity> empty() {
          return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	  }
  

	public static Specification<GovioMessageEntity> fromScheduledExpeditionDate(OffsetDateTime scheduledExpeditionDateFrom) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
        	cb.greaterThanOrEqualTo(root.get(GovioMessageEntity_.scheduledExpeditionDate), scheduledExpeditionDateFrom);
	}


	public static Specification<GovioMessageEntity> toScheduledExpeditionDate(OffsetDateTime scheduledExpeditionDateTo) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
    		cb.lessThanOrEqualTo(root.get(GovioMessageEntity_.scheduledExpeditionDate), scheduledExpeditionDateTo);
	}


	public static Specification<GovioMessageEntity> fromExpeditionDate(OffsetDateTime expeditionDateFrom) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.greaterThanOrEqualTo(root.get(GovioMessageEntity_.expeditionDate), expeditionDateFrom);
	}


	public static Specification<GovioMessageEntity> toExpeditionDate(OffsetDateTime expeditionDateTo) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.lessThanOrEqualTo(root.get(GovioMessageEntity_.expeditionDate), expeditionDateTo);
	}


	public static Specification<GovioMessageEntity> byTaxCode(String taxCode) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(root.get(GovioMessageEntity_.taxcode), taxCode);
	}


	public static Specification<GovioMessageEntity> byServiceId(Long serviceId) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(
					root.get(GovioMessageEntity_.govioServiceInstance)
							.get(ServiceInstanceEntity_.service)
							.get(GovioServiceEntity_.govhubService)
							.get(ServiceEntity_.id),
					serviceId);
	}


	public static Specification<GovioMessageEntity> byOrganizationId(Long organizationId) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(
					root.get(GovioMessageEntity_.govioServiceInstance)
							.get(ServiceInstanceEntity_.organization)
							.get(OrganizationEntity_.id),
					organizationId);
	}
	
	
	private GovioMessageFilters() { }
	
	
}
