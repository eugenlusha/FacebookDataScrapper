package ExtractedData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoCommentCoReactUsers {
    private ExtractedCommentReactUsers user1;
    private ExtractedCommentReactUsers user2;
}
