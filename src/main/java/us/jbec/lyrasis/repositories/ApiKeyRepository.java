package us.jbec.lyrasis.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lyrasis.models.ApiKey;

public interface ApiKeyRepository extends CrudRepository<ApiKey, String> {
}