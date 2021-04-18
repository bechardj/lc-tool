package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.database.User;

public interface UserRepository extends CrudRepository<User, String> {

}