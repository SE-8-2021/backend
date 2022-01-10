package pvs.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectDTO {
    private Long memberId;
    private String projectName;
}
