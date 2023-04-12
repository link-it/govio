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

import java.time.OffsetDateTime;
import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.beans.MessageOrdering;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioMessageEntity_;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;

public class MessageFilters {
	

	
	public static Specification<GovioMessageEntity> empty() {
	      return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	 }
	  
	
	public static Specification<GovioMessageEntity> never() {
			return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> cb.isTrue(cb.literal(false)); 
	}
  

	public static Specification<GovioMessageEntity> fromScheduledExpeditionDate(OffsetDateTime scheduledExpeditionDateFrom) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
	    	cb.greaterThanOrEqualTo(root.get(GovioMessageEntity_.scheduledExpeditionDate), scheduledExpeditionDateFrom);
	}


	public static Specification<GovioMessageEntity> toScheduledExpeditionDate(OffsetDateTime scheduledExpeditionDateTo) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
    		cb.lessThanOrEqualTo(root.get(GovioMessageEntity_.scheduledExpeditionDate), scheduledExpeditionDateTo);
	}


	public static Specification<GovioMessageEntity> fromExpeditionDate(OffsetDateTime expeditionDateFrom) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.greaterThanOrEqualTo(root.get(GovioMessageEntity_.expeditionDate), expeditionDateFrom);
	}


	public static Specification<GovioMessageEntity> toExpeditionDate(OffsetDateTime expeditionDateTo) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.lessThanOrEqualTo(root.get(GovioMessageEntity_.expeditionDate), expeditionDateTo);
	}


	public static Specification<GovioMessageEntity> likeTaxcode(String taxCode) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(
					cb.lower(root.get(GovioMessageEntity_.taxcode)),
					"%"+taxCode.toLowerCase()+"%");
	}


	public static Specification<GovioMessageEntity> byServiceId(Long serviceId) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(
					root.get(GovioMessageEntity_.govioServiceInstance)
							.get(GovioServiceInstanceEntity_.service)
							.get(ServiceEntity_.id),
					serviceId);
	}


	public static Specification<GovioMessageEntity> byOrganizationId(Long organizationId) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.equal(
					root.get(GovioMessageEntity_.govioServiceInstance)
							.get(GovioServiceInstanceEntity_.organization)
							.get(OrganizationEntity_.id),
					organizationId);
	}
	
	
	public static Specification<GovioMessageEntity> byServiceIds(Collection<Long > serviceIds) {
		if (serviceIds.isEmpty()) {
			return never();
		} else {
			return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
						root.get(GovioMessageEntity_.govioServiceInstance)
								.get(GovioServiceInstanceEntity_.service)
								.get(ServiceEntity_.id).in(serviceIds);
		}
	}
	
	
	public static Specification<GovioMessageEntity> byOrganizationIds(Collection<Long> organizationIds) {
		if (organizationIds.isEmpty()) {
			return never();
		} else {
			return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
						root.get(GovioMessageEntity_.govioServiceInstance)
								.get(GovioServiceInstanceEntity_.organization)
								.get(OrganizationEntity_.id).in(organizationIds);
		}
	}
	
	
	public static Specification<GovioMessageEntity> likeServiceName(String serviceQ) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
			cb.like(
					cb.lower(
							root.get(GovioMessageEntity_.govioServiceInstance)
							.get(GovioServiceInstanceEntity_.service)
							.get(ServiceEntity_.name)),
					"%"+serviceQ.toLowerCase()+"%");
	}


	public static Specification<GovioMessageEntity> likeOrganizationName(String organizationQ) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
		cb.like(
				cb.lower(
						root.get(GovioMessageEntity_.govioServiceInstance)
						.get(GovioServiceInstanceEntity_.organization)
						.get(OrganizationEntity_.legalName)),
				"%"+organizationQ.toLowerCase()+"%");
	}


	public static Specification<GovioMessageEntity> likeOrganizationTaxCode(String organizationQ) {
		return (Root<GovioMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
		cb.like(
				cb.lower(
						root.get(GovioMessageEntity_.govioServiceInstance)
						.get(GovioServiceInstanceEntity_.organization)
						.get(OrganizationEntity_.taxCode)),
				"%"+organizationQ.toLowerCase()+"%");
	}
	

	public static Sort sort(MessageOrdering sort, Direction direction) {
		
		switch (sort) {
		case SCHEDULED_EXPEDITION_DATE:
			return Sort.by(direction, GovioMessageEntity_.SCHEDULED_EXPEDITION_DATE);
		case ID:
			return Sort.by(direction, GovioMessageEntity_.ID);
		case UNSORTED:
			return Sort.unsorted();
		default:
			throw new UnreachableException();
		}
	}

	
	private MessageFilters() { }



	
}
