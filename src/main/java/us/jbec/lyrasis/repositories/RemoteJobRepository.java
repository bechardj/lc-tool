package us.jbec.lyrasis.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import us.jbec.lyrasis.models.RemotelySubmittedJob;

import java.util.List;

public interface RemoteJobRepository extends CrudRepository<RemotelySubmittedJob, Integer> {

    @Query(value = "SELECT A.id, A.api_key, A.job_id, A.json, A. submit_time FROM remotely_submitted_job A " +
            "WHERE A.submit_time = (SELECT MAX(B.submit_time) from remotely_submitted_job B where B.job_id = A.job_id);",
            nativeQuery = true)
    List<RemotelySubmittedJob> selectNewestJobsBySubmitTime();
}