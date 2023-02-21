package it.govio.batch.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "govio_file_message_stats")
public class FileMessageStats {
	
	GovioFileEntity file;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	GovioMessageEntity.Status status;
	
	Long count;
	
}
