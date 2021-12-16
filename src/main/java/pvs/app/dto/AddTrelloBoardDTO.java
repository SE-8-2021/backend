package pvs.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AddTrelloBoardDTO {
    private Long projectId;
    private String repositoryURL;
}
