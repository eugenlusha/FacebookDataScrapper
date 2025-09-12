package ExtractedData;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor

public class ExtractedComments {
    String graphNodeId;
    String commentId;
    public Date startDate;
    String authorName;
    String authorId;
    String authorProfileUrl;
    String commentText;
    List<String> attachmentUrl;
    String commentUrl;
    Integer commentReactionCount;
    String commentTime;
    String owningPostId;
    String parentShareFbid;
    ArrayList<ExtractedComments> replyComments;

    public ExtractedComments() {
        this.replyComments = new ArrayList<>();
        this.attachmentUrl = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if(this.hashCode() == o.hashCode()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, authorName, authorId, commentText);
    }
}
