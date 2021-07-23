package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import us.jbec.lct.models.database.InvitationRecord;

import java.util.List;

/**
 * Repository interface for providing default CRUD operations native queries
 */
public interface InvitationRepository extends CrudRepository<InvitationRecord, Long> {

    @Query(value = "select * from invitation_record A where A.email = :email",
            nativeQuery = true)
    List<InvitationRecord> selectInvitationByEmail(@Param("email") String email);
}