package it.govio.msgverified.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import it.govio.msgverified.entity.GovioMessageEntity;


public interface GovioMessagesRepository extends JpaRepository<GovioMessageEntity, Long> {
	 @Query("SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE (msg.status = ?1 OR msg.status = ?2 OR msg.status =?3) AND (msg.expeditionDate < ?4 OR msg.lastUpdateStatus < ?4)")
	public Page<GovioMessageEntity> findByStatus(String status1, String status2, String status3,Pageable pageable);
}