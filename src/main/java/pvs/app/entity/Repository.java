package pvs.app.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Repository {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long repositoryId;

    @NotNull
    private String url;

    @NotNull
    private String type;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
    @ToString.Exclude
    private Set<GithubCommit> githubCommitSet;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "repository")
    @ToString.Exclude
    private Set<GitlabCommit> gitlabCommitSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Repository that = (Repository) o;
        return repositoryId != null && Objects.equals(repositoryId, that.repositoryId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
