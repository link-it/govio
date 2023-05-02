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
package it.govio.batch.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.query.Param;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;

public interface GovioFilesRepository extends JpaRepositoryImplementation<GovioFileEntity, Long> {

	@Query("SELECT f FROM GovioFileEntity f WHERE f.status = :status")
	List<GovioFileEntity> findByStatus(@Param("status") Status status, Pageable pageable);
	
    @Modifying
    @Query("UPDATE GovioFileEntity f SET f.status = :newStatus WHERE f.status = :oldStatus")
    int updateAllStatus(@Param("oldStatus") Status oldStatus, @Param("newStatus") Status newStatus);
    
    @Modifying
    @Query("UPDATE GovioFileEntity f SET f.status = :newStatus WHERE f.id IN  :ids")
    int updateStatus(@Param("ids") List<Long> ids, @Param("newStatus") Status newStatus);

    @Modifying
    @Query(value = "UPDATE govio_files "
    		+ "SET status='PROCESSED', "
    		+ "acquired_messages = (SELECT count(*) FROM govio_file_messages WHERE id_govio_file=govio_files.id and id_govio_message is not null), "
    		+ "error_messages = (SELECT count(*) FROM govio_file_messages WHERE id_govio_file=govio_files.id and id_govio_message is null) "
    		+ "where status='PROCESSING'",
		 nativeQuery = true)
	void updateProcessedFiles();
}
