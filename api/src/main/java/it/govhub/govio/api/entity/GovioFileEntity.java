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
package it.govhub.govio.api.entity;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.utils.JpaPathConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "govio_files")
public class GovioFileEntity {

	public enum Status {CREATED, PROCESSING, PROCESSED}

	@Id 
	@SequenceGenerator(name="seq_govio_files",sequenceName="seq_govio_files", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_files")
	private Long id;

	@ManyToOne
 	@JoinColumn(name = "id_govhub_user", nullable=false, foreignKey = @ForeignKey(name = "GovioFile_GovhubUser"))
	private UserEntity govauthUser;
	
	@ManyToOne
	@JoinColumn(name = "id_govio_service_instance", nullable = false, foreignKey = @ForeignKey(name = "GovioFile_GovioServiceInstance"))
	private GovioServiceInstanceEntity serviceInstance;
	
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "creation_date", nullable = false)
	private OffsetDateTime creationDate;
	
	@Column(name = "processing_date")
	private OffsetDateTime processingDate;
	
	@Convert(converter = JpaPathConverter.class)
	@Column(name = "location",  length = 2048, nullable = false)
	private Path location;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "status_detail",  length = 1024)
	private String statusDetail;
	
	@Column(name = "size", nullable = false)
	private Long size;

	@Column(name = "acquired_messages")
	private Long acquiredMessages;

	@Column(name = "error_messages")
	private Long errorMessages;
	
	//TODO Da verificare!! Lorenzo vs Francesco
//	// L'entita collegata è readonly, non abbiamo cascade quindi possiamo salvare il GovioFileEntity senza problemi
	@OneToMany(mappedBy = "govioFile")
	@Builder.Default
	private List<GovioFileMessageEntity> fileMessages = new ArrayList<>();
}
