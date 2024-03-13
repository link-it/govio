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
package it.govhub.govio.api.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;

@Component
public class GovioConfig  implements ApplicationConfig {
	
	@Value("${application-id:govio}")
	private String applicationId;
	
	public static final Set<String> DEFAULT_READ_ORGANIZATION_ROLES = Set.of(

			GovioRoles.GOVIO_SYSADMIN, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,
			GovioRoles.GOVIO_VIEWER, 
			GovioRoles.GOVIO_SENDER);
	
	public static final Set<String> DEFAULT_READ_SERVICE_ROLES = Set.of(
			GovioRoles.GOVIO_SYSADMIN, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER,
			GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,
			GovioRoles.GOVIO_VIEWER, 
			GovioRoles.GOVIO_SENDER);

	@Override
	public String getApplicationId() {
		return this.applicationId;
	}

	@Override
	public Set<String> getReadServiceRoles() {
		return DEFAULT_READ_ORGANIZATION_ROLES;
	}

	@Override
	public Set<String> getReadOrganizationRoles() {
		return DEFAULT_READ_SERVICE_ROLES;
	}

}