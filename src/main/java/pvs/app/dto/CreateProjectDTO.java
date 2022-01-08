package pvs.app.dto;

import lombok.Data;

@Data
public class CreateProjectDTO {
    private Long memberId;
    private String projectName;
    private String githubRepositoryURL;
    private String gitLabRepositoryURL;
    private String sonarRepositoryURL;
    private String trelloBoardURL;
}
