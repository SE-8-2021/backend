package pvs.app.dto;

import lombok.Data;

@Data
public class CreateProjectDTO {
    private Long memberId;
    private String projectName;
}
