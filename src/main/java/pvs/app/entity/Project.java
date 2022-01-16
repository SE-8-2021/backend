package pvs.app.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Project {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long projectId;

    @NotNull
    private Long memberId;

    @NotNull
    private String name;

    @NotNull
    private String avatarURL = "";

    // default value is false
    // getter of "boolean" type is "isRemoved()"
    @NotNull
    private boolean removed = false;

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_repository",
            joinColumns = {@JoinColumn(name = "project_id")},
            inverseJoinColumns = {@JoinColumn(name = "repository_id")}
    )
    private Set<Repository> repositorySet = new HashSet<>();
}
