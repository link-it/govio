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
package it.govio.batch.step.beans;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileMessageEntity;

@Component
public class GovioFileMessageLineMapper implements LineMapper<GovioFileMessageEntity> {
	
	public GovioFileMessageEntity mapLine(String line, int lineNumber) throws Exception {
		return GovioFileMessageEntity.builder()
				.lineNumber(Long.valueOf(lineNumber))
				.lineRecord(line)
				.build();
	}
}