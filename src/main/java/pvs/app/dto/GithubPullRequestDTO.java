package pvs.app.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

@Data
public class GithubPullRequestDTO {
    private String repoOwner;
    private String repoName;
    private Date createdAt;
    private Date closedAt;
    private Date mergedAt;

    public void setCreatedAt(JsonNode createdAt) {
        DateTimeFormatter isoParser = ISODateTimeFormat.dateTimeNoMillis();
        this.createdAt = isoParser.parseDateTime(createdAt.toString().replace("\"", "")).toDate();
    }

    public void setClosedAt(JsonNode closedAt) {
        if (closedAt != null && closedAt.textValue() != null) {
            DateTimeFormatter isoParser = ISODateTimeFormat.dateTimeNoMillis();
            this.closedAt = isoParser.parseDateTime(closedAt.toString().replace("\"", "")).toDate();
        }
    }

    public void setMergedAt(JsonNode mergedAt) {
        if (mergedAt != null && mergedAt.textValue() != null) {
            DateTimeFormatter isoParser = ISODateTimeFormat.dateTimeNoMillis();
            this.mergedAt = isoParser.parseDateTime(mergedAt.toString().replace("\"", "")).toDate();
        }
    }
}
