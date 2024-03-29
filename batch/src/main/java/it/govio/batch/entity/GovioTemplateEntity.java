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
package it.govio.batch.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "govio_templates")
public class GovioTemplateEntity  {
	
	@Id
	@SequenceGenerator(name="seq_govio_templates",sequenceName="seq_govio_templates", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_templates")
	private Long id;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	
	@Column(name = "subject", nullable = false)
	private String subject;
	
	@Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
	private String messageBody;

	@Column(name = "has_due_date", nullable = false)
	private Boolean hasDueDate;

	@Column(name = "has_payment", nullable = false)
	private Boolean hasPayment;
	
	@OneToMany(mappedBy = "govioTemplate", fetch = FetchType.EAGER)
	Set<GovioTemplatePlaceholderEntity> govioTemplatePlaceholders;
}
