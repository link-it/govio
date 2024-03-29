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

/**
 * Motivazioni di questa implementazione non intrusiva, cioè con una tabella e un'entità a parte:
 *		- Le query più frequenti gestiscono meno dati: Le operazioni di lettura, e di scrittura in batch sono molto più frequenti di quelle di creazione del singolo messaggio e non utilizzano 
 *		  le informazioni sulla idempotency key.	
 *		Consente di delineare una procedura per aggiungere una IdempotencyKey anche per altre risorse. All'inizio con copia incolla, poi abbiamo gli strumenti per metterla in una libreria
 *     a parte e poter aggiungere idempotency key a piacere.
 *
 */
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
