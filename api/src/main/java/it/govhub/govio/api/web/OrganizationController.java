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
package it.govhub.govio.api.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;

@V1RestController
public class OrganizationController  implements OrganizationApi {
	
	@Autowired
	ReadOrganizationController readOrganizationController;

	@Override
	public ResponseEntity<Resource> downloadOrganizationLogo(Long id) {
		return this.readOrganizationController.downloadOrganizationLogo(id);
	}


	@Override
	public ResponseEntity<Resource> downloadOrganizationLogoMiniature(Long id) {
		return this.readOrganizationController.downloadOrganizationLogoMiniature(id);
	}


	@Override
	public ResponseEntity<OrganizationList> listOrganizations(OrganizationOrdering sort, Direction sortDirection, Integer limit, Long offset, String q, List<String> withRoles) {
		return this.readOrganizationController.listOrganizations(sort, sortDirection, limit, offset, q, withRoles);
	}


	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		return this.readOrganizationController.readOrganization(id);
	}

}
