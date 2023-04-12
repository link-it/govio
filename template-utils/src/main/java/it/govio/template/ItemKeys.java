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
package it.govio.template;

public enum ItemKeys {
	TAXCODE("taxcode"), 
	EXPEDITIONDATE("expedition_date"), 
	DUEDATE("due_date"), 
	NOTICENUMBER("notice_number"), 
	AMOUNT("amount"), 
	INVALIDAFTERDUEDATE("invalid_after_due_date"), 
	PAYEE("payee");

	private String string;

	ItemKeys(String string) {
		this.string = string;
	}  

	@Override
	public String toString() {
		return string;
	}
}
