/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
	
	
	private PlaceholderFilters() {}
}
