package VideoPack; 
import java.util.ArrayList;

public class Representation{
    public String representation_id;
    public String mime_type;
    public String codecs;
    public String base_url;
    public int bandwidth;
    public int height;
    public int width;
    public String playback_resolution_mos;
    public String playback_resolution_csvqm;
    public ArrayList<Segment> segments;
}
