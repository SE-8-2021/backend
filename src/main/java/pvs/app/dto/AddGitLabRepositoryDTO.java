package pvs.app.dto;

import lombok.Data;

@Data
public class AddGitLabRepositoryDTO {
    private Long projectId;
    private String repositoryURL;
}
