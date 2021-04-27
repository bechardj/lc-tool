package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.Role;

/**
 * Repository interface for providing default CRUD operations
 */
public interface RoleRepository extends CrudRepository<Role, String> {
}