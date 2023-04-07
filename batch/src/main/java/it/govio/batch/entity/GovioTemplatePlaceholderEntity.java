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


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "govio_template_placeholders")
public class GovioTemplatePlaceholderEntity {

	@EmbeddedId
	private GovioTemplatePlaceholderKey id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@MapsId("GovioTemplateId")
	@JoinColumn(name = "id_govio_template", nullable = false, foreignKey = @ForeignKey(name = "GovioTemplatePlaceholder_GovioTemplate"))
	private GovioTemplateEntity govioTemplate;

	@ManyToOne()
	@MapsId("GovioPlaceholderId")
	@JoinColumn(name = "id_govio_placeholder", nullable = false, foreignKey = @ForeignKey(name = "GovioTemplatePlaceholder_GovioPlaceholder"))
	private GovioPlaceholderEntity govioPlaceholder;

	@Column(name = "position", nullable = false)
	private int position;

	@Column(name = "mandatory", nullable = false)
	private boolean mandatory;

}

@Setter
@Getter	
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
class GovioTemplatePlaceholderKey implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "id_govio_placeholder")
	private Long govioPlaceholder;

	@Column(name = "id_govio_template")
	private Long govioTemplate;
}



