package it.govio.batch.entity;



import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
	@SequenceGenerator(name="seq_govio_messages",sequenceName="seq_govio_messages", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_messages")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_message", nullable = false)
	private GovioMessageEntity govioMessage;

	@Column(name = "line_record")
	private String line_record;
	
	@Column(name = "line_number")
	private Long line_number;

	@Column(name = "error")
	private String error;

}