package it.govhub.govio.api.repository;

import java.time.OffsetDateTime;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.beans.MessageOrdering;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioMessageEntity_;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;

public class MessageFilters {
	

	
	public static Specification<GovioMessageEntity> empty() {
	      return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	 }
	  
	
	public static Specification<GovioMessageEntity> never() {
			return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isTrue(cb.literal(false)); 
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
							.get(GovioServiceInstanceEntity_.service)
							.get(ServiceEntity_.id),
					serviceId);
	}


	public static Specification<GovioMessageEntity> byOrganizationId(Long organizationId) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(
					root.get(GovioMessageEntity_.govioServiceInstance)
							.get(GovioServiceInstanceEntity_.organization)
							.get(OrganizationEntity_.id),
					organizationId);
	}
	
	
	public static Specification<GovioMessageEntity> byServiceIds(Collection<Long > serviceIds) {
		if (serviceIds.isEmpty()) {
			return never();
		} else {
			return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
						root.get(GovioMessageEntity_.govioServiceInstance)
								.get(GovioServiceInstanceEntity_.service)
								.get(ServiceEntity_.id).in(serviceIds);
		}
	}
	
	
	public static Specification<GovioMessageEntity> byOrganizationIds(Collection<Long> organizationIds) {
		if (organizationIds.isEmpty()) {
			return never();
		} else {
			return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
						root.get(GovioMessageEntity_.govioServiceInstance)
								.get(GovioServiceInstanceEntity_.organization)
								.get(OrganizationEntity_.id).in(organizationIds);
		}
	}
	

	public static Sort sort(MessageOrdering sort, Direction direction) {
		
		switch (sort) {
		case SCHEDULED_EXPEDITION_DATE:
			return Sort.by(direction, GovioMessageEntity_.SCHEDULED_EXPEDITION_DATE);
		case ID:
			return Sort.by(direction, GovioMessageEntity_.ID);
		case UNSORTED:
			return Sort.unsorted();
		default:
			throw new UnreachableException();
		}
	}

	
	private MessageFilters() { }
	
}
