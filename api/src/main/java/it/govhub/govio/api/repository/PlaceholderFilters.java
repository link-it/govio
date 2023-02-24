package it.govhub.govio.api.repository;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.entity.GovioPlaceholderEntity_;

public class PlaceholderFilters {
	
	public static Specification<GovioPlaceholderEntity> empty() {
		return (Root<GovioPlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null;
	}
	
	
	public static Specification<GovioPlaceholderEntity> likeName(String name) {
		return (Root<GovioPlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(cb.upper(root.get(GovioPlaceholderEntity_.name)), "%"+name.toUpperCase()+"%");
	}
	
	
	public static Specification<GovioPlaceholderEntity> likeDescription(String description) {
		return (Root<GovioPlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(cb.upper(root.get(GovioPlaceholderEntity_.description)), "%"+description.toUpperCase()+"%");
	}


	public static Specification<GovioPlaceholderEntity> byIds(Set<Long> requestedIds) {
		return (Root<GovioPlaceholderEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			root.get(GovioPlaceholderEntity_.id).in(requestedIds);
	}
	
}
