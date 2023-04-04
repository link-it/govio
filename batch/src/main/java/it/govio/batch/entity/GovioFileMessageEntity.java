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
package it.govio.batch.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "govio_file_messages")
public class GovioFileMessageEntity {

	@Id
	@SequenceGenerator(name="seq_govio_file_messages",sequenceName="seq_govio_file_messages", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_file_messages")
	private Long id;

	@Column(name = "error", columnDefinition = "TEXT")
	private String error;

	@Column(name = "line_record", columnDefinition = "TEXT")
	private String lineRecord;

	@Column(name = "line_number")
	private Long lineNumber;

	@ManyToOne
	@JoinColumn(name = "id_govio_file", nullable = false, foreignKey = @ForeignKey(name = "GovioFileMessage_GovioFile"))
	private GovioFileEntity govioFile;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "id_govio_message", nullable = true, foreignKey = @ForeignKey(name = "GovioFileMessage_GovioMessage"))
	private GovioMessageEntity govioMessage;

}
