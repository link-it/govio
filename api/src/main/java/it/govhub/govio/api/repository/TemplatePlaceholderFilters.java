/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
	
	private TemplatePlaceholderFilters() {
		// donothing
	}
	
}
