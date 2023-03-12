package it.govhub.govio.api.repository;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govio.api.entity.GovioTemplateEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;

public class ServiceInstanceFilters {

	public static Specification<GovioServiceInstanceEntity> empty() {
           return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
   }
	

	public static Specification<GovioServiceInstanceEntity> byOrganizationId(Long organizationId) {
        return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
        	cb.equal(root.get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.id), organizationId); 
	}
	

	public static Specification<GovioServiceInstanceEntity> byServiceId(Long serviceId) {
        return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
        	cb.equal(root.get(GovioServiceInstanceEntity_.service).get(ServiceEntity_.id), serviceId); 
	}

	
	public static Specification<GovioServiceInstanceEntity> byOrganizationIds(Collection<Long> orgIds) {
        return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
        	if (orgIds.isEmpty() ) {
        		return cb.isFalse(cb.literal(true));
        	}
        	else {        	
        		return root.get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.id).in(orgIds);
        	}
        };
	}


	public static Specification<GovioServiceInstanceEntity> byServiceIds(Collection<Long> serviceIds) {
		 return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
	        	if (serviceIds.isEmpty() ) {
	        		return cb.isFalse(cb.literal(true));
	        	}
	        	else {
	        		return root.get(GovioServiceInstanceEntity_.service).get(ServiceEntity_.id).in(serviceIds);
	        	}
		 };
	}
	   

	public static Specification<GovioServiceInstanceEntity> likeServiceName(String serviceQ) {
        return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
        	cb.like(
        			cb.lower(root.get(GovioServiceInstanceEntity_.service).get(ServiceEntity_.name)), 
        			"%"+serviceQ.toLowerCase()+"%" );
	}
	
	
	public static Specification<GovioServiceInstanceEntity> likeTemplateName(String templateQ) {
        return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
        	cb.like(
        			cb.lower(root.get(GovioServiceInstanceEntity_.template).get(GovioTemplateEntity_.name)),
        			"%"+templateQ.toLowerCase()+"%" );
	}


	public static Specification<GovioServiceInstanceEntity> likeOrganizationName(String organizationQ) {
	    return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
    	cb.like(
    			cb.lower(root.get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.legalName)), 
    			"%"+organizationQ.toLowerCase()+"%" );
	}
	
	public static Specification<GovioServiceInstanceEntity> likeOrganizationTaxCode(String q) {
	    return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
    	cb.like(
    			cb.lower(root.get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.taxCode)),
    			"%"+q.toLowerCase()+"%" );
	}
	
	
	private ServiceInstanceFilters() {	}

}