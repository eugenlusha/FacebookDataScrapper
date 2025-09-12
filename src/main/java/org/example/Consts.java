package org.example;

import java.util.Arrays;
import java.util.List;

public class Consts {

    public static final List<String> postsFields = Arrays.asList("GraphId", "Id","StroyId","StoryUri","TypeName","StoryDescription","ReactionCount","HasVideoAttachment","HasPhotoAttachment","PhotosIds","VideosIds","VideoLink","VideoOwnerId","VideoComment");
    public static final List<String> commentsFields = Arrays.asList("GraphId", "CommentId","authorName","authorId","authorProfileUrl","commentText","attachmentUrl","commentUrl","commentReactionCount","commentTime","owningPostId","parentShareFbid","replyComments");
    public static final List<String> reactionsFields = Arrays.asList("GraphId", "ReactionId","AuthorName","AuthorProfileUrl","ReactionImageUrl");
    public static final List<String> usersFields = Arrays.asList("GraphId", "UserId","UserName","UserProfileUrl");
}
