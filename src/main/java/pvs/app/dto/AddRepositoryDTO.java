package pvs.app.dto;

import lombok.Data;

@Data
public class AddRepositoryDTO {
    private Long projectId;
    private String repositoryURL;
}
