package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.Role;

public interface RoleRepository extends CrudRepository<Role, String> {
}