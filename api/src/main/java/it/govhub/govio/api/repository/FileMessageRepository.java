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
package it.govhub.govio.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.entity.GovioFileMessageEntity_;
import it.govhub.govio.api.entity.GovioMessageEntity_;
import it.govhub.govio.api.repository.model.MessageStatusCount;

public interface FileMessageRepository extends JpaRepositoryImplementation<GovioFileMessageEntity, Long> {

    @EntityGraph(attributePaths = {
    		GovioFileMessageEntity_.GOVIO_MESSAGE,
    		GovioFileMessageEntity_.GOVIO_MESSAGE+"."+GovioMessageEntity_.IDEMPOTENCY_KEY
    })
	public Page<GovioFileMessageEntity> findAll(Specification<GovioFileMessageEntity> spec, Pageable pageable);
    
    
    @Query("SELECT new it.govhub.govio.api.repository.model.MessageStatusCount(m.status, COUNT(fm.id)) "
    		  + "FROM GovioFileMessageEntity fm JOIN fm.govioFile f LEFT JOIN fm.govioMessage m WHERE f.id=:id GROUP BY m.status ORDER BY m.status DESC")
    public List<MessageStatusCount> countTotalFileMessageByStatus(Long id);

}

