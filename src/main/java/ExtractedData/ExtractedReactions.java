package ExtractedData;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ExtractedReactions {
    String graphNodeId;
    String reactionId;
    String authorName;
    String authorProfileUrl;
    String reactionImageUrl;
    public Date startDate;


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
        return Objects.hash(authorName, authorProfileUrl, reactionImageUrl);
    }
}
