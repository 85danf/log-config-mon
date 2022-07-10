package org.danf.configmon.persistence;

import org.danf.configmon.model.FlowLogConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * Persistence notes:
 * - I have opted to use embedded derby for simplicity
 * - In general, I don't like using jpa/hibernate since IMHO they are only good until you need to do something that's just a bit non-standard (or want joins to be performant)
 * - Its also possible to use a nosql db here, but since I'm filtering based on the time field that would imply a full scan (since it's not a key and is modified often).
 */
@Repository
public interface FlowLogConfigRepository extends JpaRepository<FlowLogConfig, Long> {

    List<FlowLogConfig> findByLastCheckedLessThanEqual(Date lastChecked);

    @Modifying
    @Transactional
    @Query("UPDATE FlowLogConfig flc set flc.lastChecked = :lastChecked WHERE flc.id = :id")
    int setLastChecked(@Param("id") long id, @Param("lastChecked") Date lastChecked);

}
