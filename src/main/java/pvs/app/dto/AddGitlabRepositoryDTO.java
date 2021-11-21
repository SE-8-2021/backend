package pvs.app.dto;

import lombok.Data;

@Data
public class AddGitlabRepositoryDTO {
    private Long projectId;
    private String repositoryURL;
}
