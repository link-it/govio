package it.govio.batch.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
	@Table(name = "govio_placeholders")
	public class GovioPlaceholderEntity {
		
		enum Type { STRING, DATE, DATETIME };
		
		@Id
		@SequenceGenerator(name="seq_govio_placeholders",sequenceName="seq_govio_placeholders", initialValue=1, allocationSize=1)
		@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_placeholders")
		private Long id;

		@Column(name = "name", nullable = false)
		private String name;
		
		@Column(name = "description")
		private String description;
		
		@Column(name = "example", nullable = false)
		private String example;
		
		@Column(name = "type", nullable = false)
		private String type;
		// private Type type; nella query prendeva il valore come intero e non come enum
		
		@Column(name = "pattern")
		private String pattern;
}
