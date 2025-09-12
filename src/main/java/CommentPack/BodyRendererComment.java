package CommentPack; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

public class BodyRendererComment {
    public String __typename;
    public ArrayList<Object> delight_ranges;
    public ArrayList<Object> image_ranges;
    public ArrayList<Object> inline_style_ranges;
    public ArrayList<Object> aggregated_ranges;
    public ArrayList<Range> ranges;
    public ArrayList<Object> color_ranges;
    public String text;
}
