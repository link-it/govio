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
package it.govhub.govio.api.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(
		name = "govio_messages_idempotency_keys", 
		uniqueConstraints = {
		   @UniqueConstraint(name = "UniqueIdempotencykeyHashcode", columnNames = {"idempotency_key", "bean_hashcode"})
		},
		indexes = @Index(columnList = "bean_hashcode", name = "BeanHashcodeIdx"))
public class GovioMessageIdempotencyKeyEntity {
	
	// L'id del govio_message associato
	@Id
	@Column(name = "govio_message_id")
	private Long id;
	
	// Con @MapsId copiamo i valori della chiave primaria da quelli della GovioMessageEntity.
	// In questo modo L'id di una IdempotencyKeyMessageEntity è lo stesso della GovioMessageEntity
	@OneToOne
	@MapsId				
	@JoinColumn( name = "id_govio_message", foreignKey = @ForeignKey(name="IdempotencyKey_GovioMessage"))
	private GovioMessageEntity message;
	
	// La chiave di idempotenza usata durante una richiesta
	
	@Column(name = "idempotency_key", columnDefinition = "uuid")
	private UUID idempotencyKey;
	
	// L'hashcode del bean inviato durante la richeista
	
	@Column(name = "bean_hashcode", columnDefinition = "BIGINT")
	private Integer beanHashcode;

}
