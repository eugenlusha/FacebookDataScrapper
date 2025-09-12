package ExtractedData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedCommentReactUsers {
    String graphNodeId;
    String authorName;
    String authorId;
    String authorProfileUrl;
    List<Integer> listOfPostsCommentedTo;
    List<Integer> listOfPostsReactedTo;

    public ExtractedCommentReactUsers(String graphNodeId,  String authorName, String authorId, String authorProfileUrl) {
        this.graphNodeId = graphNodeId;
        this.authorName = authorName;
        this.authorId = authorId;
        this.authorProfileUrl = authorProfileUrl;
    }

    @Override
    public boolean equals(Object o) {
        ExtractedCommentReactUsers that = (ExtractedCommentReactUsers) o;
        return Objects.equals(authorId, that.authorId) || Objects.equals(authorProfileUrl, that.authorProfileUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorProfileUrl);
    }
}
