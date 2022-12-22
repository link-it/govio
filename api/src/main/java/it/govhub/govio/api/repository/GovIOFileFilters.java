package it.govhub.govio.api.repository;

import java.time.OffsetDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovIOFileEntity;
import it.govhub.govio.api.entity.GovIOFileEntity_;
import it.govhub.govio.api.entity.ServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.entity.UserEntity_;

public class GovIOFileFilters {
	
	public static Specification<GovIOFileEntity> empty() {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}
	
	
	public static Specification<GovIOFileEntity> byOrganization(Long orgId) {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(root.get(GovIOFileEntity_.serviceInstance).get(ServiceInstanceEntity_.organization).get(OrganizationEntity_.id), orgId); 
	}
	
	
	public static Specification<GovIOFileEntity> byService(Long serviceId) {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.equal(root.get(GovIOFileEntity_.serviceInstance).get(ServiceInstanceEntity_.service).get(ServiceEntity_.id), serviceId);
	}
	
	
	public static Specification<GovIOFileEntity> byUser(Long userId) {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(root.get(GovIOFileEntity_.govauthUser).get(UserEntity_.id), userId);
	}
	
	public static Specification<GovIOFileEntity> likeFileName(String fileName) {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(cb.upper(root.get(GovIOFileEntity_.name)), "%" + fileName.toUpperCase() + "%");
	}
	
	public static Specification<GovIOFileEntity> fromCreationDate(OffsetDateTime creationDateFrom) {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.greaterThanOrEqualTo(root.get(GovIOFileEntity_.creationDate), creationDateFrom);
	}
	
	public static Specification<GovIOFileEntity> untilCreationDate(OffsetDateTime creationDateTo) {
		return (Root<GovIOFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			cb.lessThanOrEqualTo(root.get(GovIOFileEntity_.creationDate), creationDateTo);
	}

	
	public static Sort sort(Direction direction) {
		return Sort.by(direction, GovIOFileEntity_.NAME);
	}
	
	private GovIOFileFilters() {
	}

	
}
