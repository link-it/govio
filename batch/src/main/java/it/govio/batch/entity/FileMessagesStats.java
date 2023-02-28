package it.govio.batch.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "govio_file_messages_stats")
public class FileMessagesStats {
	
	@Id
	@SequenceGenerator(name="seq_govio_file_messages_stats",sequenceName="seq_govio_file_messages_stats", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_file_messages_stats")
	private Long id;
	
	@OneToOne
	GovioFileEntity file;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	GovioMessageEntity.Status status;
	
	Long count;
	
}