package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.ApiKey;

public interface ApiKeyRepository extends CrudRepository<ApiKey, String> {
}