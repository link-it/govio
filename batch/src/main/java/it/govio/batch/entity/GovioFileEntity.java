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


import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "govio_files")
public class GovioFileEntity {

	public enum Status {CREATED, PROCESSING, PROCESSED}

	@Id
	@SequenceGenerator(name="seq_govio_files",sequenceName="seq_govio_files", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_files")
	private Long id;

	@Column(name = "id_govhub_user", nullable = false)
	private Long govhubUserId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_service_instance", nullable = false, foreignKey = @ForeignKey(name = "GovioFile_GovioServiceInstance"))
	private GovioServiceInstanceEntity govioServiceInstance;
	
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "creation_date", nullable = false)
	private OffsetDateTime creationDate;
	
	@Column(name = "processing_date")
	private OffsetDateTime processingDate;
	
	@Column(name = "location", nullable = false,  length = 1024)
	private String location;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "status_detail",  length = 1024)
	private String statusDetail;

	@Column(name = "acquired_messages")
	private Long acquiredMessages;

	@Column(name = "error_messages")
	private Long errorMessages;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "govioFile", cascade = CascadeType.REMOVE)
	private List<GovioFileMessageEntity> govioFileMessageEntities;
	
	
}
