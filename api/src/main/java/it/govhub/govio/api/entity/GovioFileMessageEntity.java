package it.govhub.govio.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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

	@OneToOne
	@JoinColumn(name = "id_govio_message", nullable = true, foreignKey = @ForeignKey(name = "GovioFileMessage_GovioMessage"))
	private GovioMessageEntity govioMessage;

}
