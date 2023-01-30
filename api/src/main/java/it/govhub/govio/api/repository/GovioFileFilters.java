package it.govhub.govio.api.repository;

import java.time.OffsetDateTime;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.beans.FileOrdering;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileEntity_;
import it.govhub.govio.api.entity.GovioServiceEntity_;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.UserEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;


public class GovioFileFilters {
	
	public static Specification<GovioFileEntity> empty() {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<GovioFileEntity> never() {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isTrue(cb.literal(false)); 
	}
	
	
	public static Specification<GovioFileEntity> byOrganization(Long orgId) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(root.get(GovioFileEntity_.serviceInstance).get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.id), orgId); 
	}
	
	
	public static Specification<GovioFileEntity> byOrganizations(Collection<Long> orgIds) {
		if (orgIds.isEmpty()) {
			return never();
		} else {		
			return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 			
				root.get(GovioFileEntity_.serviceInstance).get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.id).in(orgIds);
		}
		
	}
	
	
	public static Specification<GovioFileEntity> byService(Long serviceId) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(GovioFileEntity_.serviceInstance).get(GovioServiceInstanceEntity_.service).get(GovioServiceEntity_.id), serviceId);
	}
	
	
	public static Specification<GovioFileEntity> byServices(Collection<Long> serviceIds) {
		if (serviceIds.isEmpty()) {
			return never();
		} else {
			return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
				root.get(GovioFileEntity_.serviceInstance).get(GovioServiceInstanceEntity_.service).get(GovioServiceEntity_.id).in(serviceIds);
		}
	}
	
	
	public static Specification<GovioFileEntity> byUser(Long userId) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(root.get(GovioFileEntity_.govauthUser).get(UserEntity_.id), userId);
	}
	
	
	public static Specification<GovioFileEntity> likeFileName(String fileName) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(cb.upper(root.get(GovioFileEntity_.name)), "%" + fileName.toUpperCase() + "%");
	}
	
	
	public static Specification<GovioFileEntity> fromCreationDate(OffsetDateTime creationDateFrom) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.greaterThanOrEqualTo(root.get(GovioFileEntity_.creationDate), creationDateFrom);
	}
	
	
	public static Specification<GovioFileEntity> untilCreationDate(OffsetDateTime creationDateTo) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.lessThanOrEqualTo(root.get(GovioFileEntity_.creationDate), creationDateTo);
	}

	
	public static Sort sort(Direction direction, FileOrdering orderBy) {
		switch (orderBy) {
		case CREATION_DATE:
			return Sort.by(direction, GovioFileEntity_.CREATION_DATE);
		case ID:
			return Sort.by(direction, GovioFileEntity_.ID);
		case UNSORTED:
			return Sort.unsorted();
		default:
			throw new UnreachableException();
		
		}
	}
	
	private GovioFileFilters() { }

	
}
