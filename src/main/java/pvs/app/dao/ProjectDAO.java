package pvs.app.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pvs.app.entity.Project;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ProjectDAO extends CrudRepository<Project, Long> {
    @NotNull
    List<Project> findAll();

    List<Project> findByMemberId(Long memberId);

    @Transactional
    @Modifying
    @Query("update Project p set p.name = ?1 where p.projectId = ?2")
    void renameProjectById(String name, Long projectId);
}
