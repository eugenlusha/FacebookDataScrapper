package CommentPack;

import java.util.ArrayList;

public class Feedback{
    public String id;
    public ExpansionInfo expansion_info;
    public RepliesFields replies_fields;
    public ViewerActor viewer_actor;
    public String url;
    public String __typename;
    public ArrayList<Plugin> plugins;
    public String comment_composer_placeholder;
    public Object constituent_badge_banner_renderer;
    public boolean have_comments_been_disabled;
    public boolean are_live_video_comments_disabled;
    public boolean is_viewer_muted;
    public Object comment_rendering_instance;
    public CommentsDisabledNoticeRendererComment comments_disabled_notice_renderer;
    public RepliesConnection replies_connection;
    public ParentObjectEnt parent_object_ent;
    public Object viewer_feedback_reaction_info;
    public TopReactions top_reactions;
    public Reactors reactors;
    public ArrayList<SupportedReactionInfo> supported_reaction_infos;
    public CometUfiReactionIconRendererComment comet_ufi_reaction_icon_renderer;
    public Object associated_video;
    public UnifiedReactors unified_reactors;
}
