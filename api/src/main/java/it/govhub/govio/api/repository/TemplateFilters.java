package it.govhub.govio.api.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity_;

public class TemplateFilters {

	public static Specification<GovioTemplateEntity> empty() {
		return (Root<GovioTemplateEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null;
	}
	
	
	public static Specification<GovioTemplateEntity> likeName(String name) {
		return (Root<GovioTemplateEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(cb.upper(root.get(GovioTemplateEntity_.name)), "%"+name.toUpperCase()+"%");
	}
	
	
	public static Specification<GovioTemplateEntity> likeDescription(String description) {
		return (Root<GovioTemplateEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(cb.upper(root.get(GovioTemplateEntity_.description)), "%"+description.toUpperCase()+"%");
	}
	

	private TemplateFilters() {
	}

}
