package us.jbec.lct.repositories;

import org.springframework.data.repository.CrudRepository;
import us.jbec.lct.models.DynamicTextType;
import us.jbec.lct.models.database.DynamicText;

import java.util.List;

/**
 * Repository interface for providing default CRUD operations
 */
public interface DynamicTextRepository extends CrudRepository<DynamicText, String> {

    List<DynamicText> getDynamicTextByDynamicTextTypeOrderBySortOrderDesc(DynamicTextType dynamicTextType);
}