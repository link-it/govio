package it.govhub.govio.api.repository;

import java.time.OffsetDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileEntity_;
import it.govhub.govio.api.entity.GovioServiceEntity_;
import it.govhub.govio.api.entity.ServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.UserEntity_;


public class GovioFileFilters {
	
	public static Specification<GovioFileEntity> empty() {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<GovioFileEntity> byOrganization(Long orgId) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(root.get(GovioFileEntity_.serviceInstance).get(ServiceInstanceEntity_.organization).get(OrganizationEntity_.id), orgId); 
	}
	
	
	public static Specification<GovioFileEntity> byService(Long serviceId) {
		return (Root<GovioFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(GovioFileEntity_.serviceInstance).get(ServiceInstanceEntity_.service).get(GovioServiceEntity_.id), serviceId);
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

	
	public static Sort sort(Direction direction) {
		return Sort.by(direction, GovioFileEntity_.NAME);
	}
	
	private GovioFileFilters() {
	}

	
}
