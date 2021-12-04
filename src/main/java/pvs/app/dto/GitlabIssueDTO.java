package pvs.app.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

@Data
public class GitlabIssueDTO {
    private String repoOwner;
    private String repoName;
    private Date createdAt;
    private Date closedAt;
}
