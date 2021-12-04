package pvs.app.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;
import java.util.Optional;

@Data
public class GitlabCommitDTO {

    static final Logger logger = LogManager.getLogger(GitlabCommitDTO.class.getName());

    private String repoOwner;
    private String repoName;
    private Date committedDate;
    private int additions;
    private int deletions;
    private int changeFiles;
    private String authorName;
    private String authorEmail;


    public void setCommittedDate(Date committedDate) {
        this.committedDate = committedDate;
    }


    public void setAuthor(Optional<JsonNode> authorJson) {
        authorJson.map(s -> s.get("name")).ifPresent(s -> this.authorName = s.toString());
        authorJson.map(s -> s.get("email")).ifPresent(s -> this.authorEmail = s.toString());
    }

    public void setCommittedDate(JsonNode committedDate) {

        DateTimeFormatter isoParser = ISODateTimeFormat.dateTimeNoMillis();
        this.committedDate =
                isoParser.parseDateTime(committedDate.toString().replace("\"", "")).toDate();
    }
}
