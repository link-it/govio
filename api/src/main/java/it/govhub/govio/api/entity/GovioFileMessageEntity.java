package it.govhub.govio.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import it.govhub.govregistry.commons.entity.listeners.EntityUpdateBlocker;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@Entity
@Table(name = "govio_files_messages")
@EntityListeners(EntityUpdateBlocker.class)
public class GovioFileMessageEntity {

	@Id
	private Long id;

	@Column(name = "error")
	private String error;

	@Column(name = "line_record")
	private String lineRecord;

	@Column(name = "line_number")
	private Long lineNumber;

	@ManyToOne
	@JoinColumn(name = "id_govio_file", nullable = false)
	private GovioFileEntity govioFile;

	@OneToOne
	@JoinColumn(name = "id_govio_message")
	private GovioMessageEntity govioMessage;

}
