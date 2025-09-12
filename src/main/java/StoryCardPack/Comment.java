package StoryCardPack; 
public class Comment{
    public String id;
    public int created_time;
    public String url;
    public boolean is_live_video_comment;
    public Feedback feedback;
    public boolean is_author_weak_reference;
    public String legacy_fbid;
    public Author author;
    public CommentParent comment_parent;
}
