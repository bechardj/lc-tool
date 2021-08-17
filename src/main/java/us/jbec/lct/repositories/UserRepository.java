package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.User;

import java.util.List;

/**
 * Repository interface for providing default CRUD operations
 */
public interface UserRepository extends CrudRepository<User, String> {

    List<User> findByFirebaseEmail(String firebaseEmail);

}