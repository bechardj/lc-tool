package us.jbec.lct.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import us.jbec.lct.models.database.Project;

import java.util.Optional;

public interface ProjectRepository extends CrudRepository<Project, Long> {

    @Query(value = "select * from project A where A.name = :name",
            nativeQuery = true)
    Optional<Project> selectProjectByName(@Param("name") String name);
}