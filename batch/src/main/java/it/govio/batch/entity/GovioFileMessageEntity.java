package it.govio.batch.entity;



import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.data.repository.cdi.Eager;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Component
@Table(name = "govio_files_messages")
public class GovioFileMessageEntity {

	@Id
	@SequenceGenerator(name="seq_govio_file_messages",sequenceName="seq_govio_file_messages", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_file_messages")
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_message", nullable = true)
	private GovioMessageEntity govioMessage;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_files", nullable = false)
	
	private GovioFileEntity govioFile;
	
	@Column(name = "line_record")
	private String lineRecord;
	
	@Column(name = "line_number")
	private Long lineNumber;

	@Column(name = "error")
	private String error;

}