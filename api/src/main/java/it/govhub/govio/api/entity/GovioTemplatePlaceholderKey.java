package it.govhub.govio.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class GovioTemplatePlaceholderKey implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "id_govio_placeholder")
	private Long govioPlaceholder;

	@Column(name = "id_govio_template")
	private Long govioTemplate;
}