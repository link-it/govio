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
