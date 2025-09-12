package VideoPack; 
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class CreationStory{
    public String tracking;
    public ShortFormVideoContext short_form_video_context;
    public String id;
    public String encrypted_tracking;
    public String encrypted_click_tracking;
    public String click_tracking_linkshim_cb;
    public PrivacyScope privacy_scope;
    public String post_id;
    public Object sponsored_data;
    public int creation_time;
    public Feedback feedback;
    public Object branded_content_post_info;
    public boolean can_viewer_delete;
    public boolean can_viewer_cancel_collaboration_invite;
    public boolean can_viewer_remove_collaborator;
    public boolean can_viewer_see_collaboration_invite;
    public Object legal_reporting_cta_type;
    public Object legal_reporting_uri;
    public SaveInfo save_info;
    @JsonProperty("to") 
    public Object myto;
    public Video video;
    public Object post_collaboration;
    public boolean can_viewer_remove_self_as_collaborator;
    public Object if_viewer_can_see_stars_toggle_menu_option;
    public Object serialized_frtp_identifiers;
    public Object debug_info;
    public Message message;
    public Object translated_message_for_viewer;
    public ContextualElementShortFormContext contextualElementShortFormContext;
    public ArrayList<Attachment> attachments;
}
