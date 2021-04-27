package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.User;

/**
 * Repository interface for providing default CRUD operations
 */
public interface UserRepository extends CrudRepository<User, String> {

}