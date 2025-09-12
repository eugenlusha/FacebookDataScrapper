package ExtractedData;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class ExtractedPosts {
    String id;
    String graphNodeId;
    String storyId;
    String storyUri;
    String typeName;
    String storyDescription;
    Integer reactionCount;
    boolean hasVideoAttachment;
    boolean hasPhotoAttachment;
    ArrayList<String> photosIds;
    ArrayList<String> videosIds;
    String videoLink;
    String videoOwnerId;
    String videoComment;
    Set<ExtractedComments> comments;
    Set<ExtractedReactions> reactions;
    public Date startDate;



    public ExtractedPosts() {
        this.comments = new HashSet<>();
        this.reactions = new HashSet<>();
        this.photosIds = new ArrayList<>();
        this.videosIds = new ArrayList<>();
    }
}
