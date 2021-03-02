package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import us.jbec.lct.models.RemotelySubmittedJob;

import java.util.List;

public interface RemoteJobRepository extends CrudRepository<RemotelySubmittedJob, Integer> {

    @Query(value = "SELECT A.id, A.api_key, A.job_id, A.json, A. submit_time FROM remotely_submitted_job A " +
            "WHERE A.submit_time = (SELECT MAX(B.submit_time) from remotely_submitted_job B where B.job_id = A.job_id);",
            nativeQuery = true)
    List<RemotelySubmittedJob> selectNewestJobsBySubmitTime();

    @Query(value = "select * from remotely_submitted_job A where A.api_key = :key and A.job_id = :job ",
            nativeQuery = true)
    List<RemotelySubmittedJob> selectJobByKeyAndId(@Param("key") String key, @Param("job") String jobId);

    @Modifying
    @Query(value = "insert into remotely_submitted_job_archive " +
            "(select * from remotely_submitted_job " +
            "where id in (select b.id " +
            "             from remotely_submitted_job b " +
            "             where b.job_id = job_id " +
            "               and b.api_key = api_key " +
            "               and (select count(*) " +
            "                    from remotely_submitted_job c " +
            "                    where c.job_id = b.job_id " +
            "                      and c.api_key = b.api_key " +
            "                      and c.submit_time > b.submit_time) >= 1));",
            nativeQuery = true)
    void archive();

    @Modifying
    @Query(value = "delete " +
            "    from remotely_submitted_job " +
            "    where id in (select b.id " +
            "            from remotely_submitted_job b " +
            "            where b.job_id = job_id " +
            "            and b.api_key = api_key " +
            "            and (select count(*) " +
            "    from remotely_submitted_job c " +
            "    where c.job_id = b.job_id " +
            "    and c.api_key = b.api_key " +
            "    and c.submit_time > b.submit_time) >= 1);",
            nativeQuery = true)
    void deleteOldRecords();
}