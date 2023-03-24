package it.govhub.govio.api.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioPlaceholderEntity_;
import it.govhub.govio.api.entity.GovioTemplateEntity_;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity_;

public class TemplatePlaceholderFilters {

	public static Specification<GovioTemplatePlaceholderEntity> empty() {
        return (Root<GovioTemplatePlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	}

	public static Specification<GovioTemplatePlaceholderEntity> byPlaceholderId(Long placeholderId) {
	     return (Root<GovioTemplatePlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
	     	cb.equal(root.get(GovioTemplatePlaceholderEntity_.govioPlaceholder).get(GovioPlaceholderEntity_.id), placeholderId); 
		}
	

	public static Specification<GovioTemplatePlaceholderEntity> byTemplateId(Long templateId) {
     return (Root<GovioTemplatePlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
     	cb.equal(root.get(GovioTemplatePlaceholderEntity_.govioTemplate).get(GovioTemplateEntity_.id), templateId); 
	}
	
	public static Specification<GovioTemplatePlaceholderEntity> byPosition(Integer position) {
	     return (Root<GovioTemplatePlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
	     	cb.equal(root.get(GovioTemplatePlaceholderEntity_.position), position); 
		}
	
	public TemplatePlaceholderFilters() {}
	
}
