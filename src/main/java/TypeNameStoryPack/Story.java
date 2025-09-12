package TypeNameStoryPack;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.ArrayList;

public class Story{
    public Feedback feedback;
    public CometSections comet_sections;
    public String encrypted_tracking;
    public ArrayList<Attachment> attachments;
    public Object sponsored_data;
    public Object text_format_metadata;
    public ArrayList<Actor> actors;
    public Message message;
    public String ghl_mocked_encrypted_link;
    public Object ghl_label_mocked_cta_button;
    public String wwwURL;
    public Object target_group;
    public Object attached_story;
    public String id;
    public FeedbackContext feedback_context;
    public StoryUfiContainer story_ufi_container;
    public boolean is_text_only_story;
    public ShareableFromPerspectiveOfFeedUfi shareable_from_perspective_of_feed_ufi;
    public String url;
    public String post_id;
    public String tracking;
    public Object inform_treatment_for_messaging;
    public ArrayList<Object> vote_attachments;
    public String __typename;
    public Object bumpers;
    public BrandedContentIntegrityContextTrigger branded_content_integrity_context_trigger;
    public Object message_truncation_line_limit;
    public Object referenced_sticker;
    public Object debug_info;
    public Object serialized_frtp_identifiers;
    public boolean can_viewer_see_menu;
    public Object easy_hide_button_story;
    public Object paid_partnership_label_tooltip;
    public ArrayList<SponsorTag> sponsor_tags;
    @JsonProperty("to") 
    public Object myto;
    public int creation_time;
    public Title title;
    public CometFeedUfiContainer comet_feed_ufi_container;
}
