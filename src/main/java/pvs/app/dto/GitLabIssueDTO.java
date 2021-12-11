package pvs.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
public class GitLabIssueDTO {
    private String repoOwner;
    private String repoName;
    private Date createdAt;
    private Date closedAt;
}
