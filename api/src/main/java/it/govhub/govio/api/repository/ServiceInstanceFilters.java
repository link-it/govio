package it.govhub.govio.api.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioServiceEntity_;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.entity.ServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;

public class ServiceInstanceFilters {

	public static Specification<ServiceInstanceEntity> empty() {
           return (Root<ServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
   }
	

	public static Specification<ServiceInstanceEntity> byOrganizationId(Long organizationId) {
        return (Root<ServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
        	cb.equal(root.get(ServiceInstanceEntity_.organization).get(OrganizationEntity_.id), organizationId); 
	}
	

	public static Specification<ServiceInstanceEntity> byServiceId(Long serviceId) {
        return (Root<ServiceInstanceEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
        	cb.equal(root.get(ServiceInstanceEntity_.service).get(GovioServiceEntity_.govhubService).get(ServiceEntity_.id), serviceId); 
	}
	
	   
	private ServiceInstanceFilters() {
	}

}
