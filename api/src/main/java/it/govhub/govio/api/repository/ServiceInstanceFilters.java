package it.govhub.govio.api.repository;

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioServiceEntity_;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
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
        	cb.equal(root.get(GovioServiceInstanceEntity_.service).get(GovioServiceEntity_.govhubService).get(ServiceEntity_.id), serviceId); 
	}

	
	public static Specification<GovioServiceInstanceEntity> byOrganizationIds(Collection<Long> orgIds) {
        return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
    		root.get(GovioServiceInstanceEntity_.organization).get(OrganizationEntity_.id).in(orgIds);
	}


	public static Specification<GovioServiceInstanceEntity> byServiceIds(Collection<Long> serviceIds) {
		 return (Root<GovioServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
     		root.get(GovioServiceInstanceEntity_.service).get(GovioServiceEntity_.govhubService).get(ServiceEntity_.id).in(serviceIds);
     		
	}
	   
	private ServiceInstanceFilters() {	}

}