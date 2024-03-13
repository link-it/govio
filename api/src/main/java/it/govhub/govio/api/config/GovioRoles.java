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

public class GovioRoles {

	public static final String REALM_NAME = "govio";
	public static final String GOVIO_SENDER = "govio_sender"; 
	public static final String GOVIO_VIEWER = "govio_viewer"; 
	public static final String GOVIO_SYSADMIN = "govio_sysadmin";
	public static final String GOVIO_SERVICE_INSTANCE_VIEWER = "govio_service_instance_viewer";
	public static final String GOVIO_SERVICE_INSTANCE_EDITOR = "govio_service_instance_editor";

	private GovioRoles() {	}
}
