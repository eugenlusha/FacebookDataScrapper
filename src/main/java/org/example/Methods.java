package org.example;

import CommentPack.AttachmentComment;
import CommentPack.CommentsDTO;
import CommentPack.Edge;
import ExtractedData.ExtractedCommentReactUsers;
import ExtractedData.ExtractedComments;
import ExtractedData.ExtractedPosts;
import ExtractedData.ExtractedReactions;
import ReactionPack.ReactionsDTO;
import StoryCardPack.StoryCardDTO;
import TypeNameStoryPack.TypenameStoryDTO;
import TypeNameUserPack.Node;
import TypeNameUserPack.TypenameUserTimelineFeedUnitsDTO;
import VideoPack.CreationStory;
import VideoPack.VideoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import de.sstoehr.harreader.model.HarEntry;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Methods {

    public String parseResponseToJson(HarEntry entry, ObjectMapper objectMapper) {
        String jsonStringToParse = entry.getResponse().getContent().getText();
        // Parse the string to JSON
        if (!jsonStringToParse.isEmpty()) {
            Object json = null;

            try {
                json = objectMapper.readValue(jsonStringToParse, Object.class);
                String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                return prettyJson;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return "";
    }

    public void extractDataFromReactionsDTP(ReactionsDTO reactionsDTO, ArrayList<ExtractedReactions> reactions) {
        System.out.println("Started extracting data from ReactionsDTO");
        ArrayList<ReactionPack.Edge> edges = reactionsDTO.data.node.reactors.edges;
        for (ReactionPack.Edge edge : edges) {
            try {
                ExtractedReactions newReaction = new ExtractedReactions();
                newReaction.setReactionId(edge.node.id);
                newReaction.setAuthorName(edge.node.name);
                newReaction.setAuthorProfileUrl(edge.node.url);
                newReaction.setReactionId(edge.feedback_reaction_info.id);
                newReaction.setReactionImageUrl(edge.feedback_reaction_info.face_image.uri);
                newReaction.setStartDate(reactionsDTO.startDate);
                reactions.add(newReaction);
            } catch (Exception e) {
                System.out.println("Error extracting data from ReactionsDTO");
                e.printStackTrace();
            }
        }

        System.out.println("Finished extracting data from ReactionsDTO");
    }

    public void extractDataFromCommentsDto(CommentsDTO commentsDTO, ArrayList<ExtractedComments> comments) {
        try {
            System.out.println("Started extracting data from CommentsDTO");
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            ArrayList<Edge> edges = commentsDTO.data.node.comment_rendering_instance_for_feed_location.comments.edges;
            if (!edges.isEmpty()) {
                for (Edge edge : edges) {
                    try {
                        ExtractedComments newComment = new ExtractedComments();
                        newComment.setCommentId(edge.node.id);
                        newComment.setAuthorName(edge.node.author.name);
                        newComment.setAuthorId(edge.node.author.id);
                        newComment.setAuthorProfileUrl(edge.node.author.url);
                        if (edge.node.body != null) {
                            newComment.setCommentText(edge.node.body.text);
                        }
                        if (edge.node.attachments != null) {
                            for (AttachmentComment att : edge.node.attachments) {
                                if (att.style_type_renderer.attachment.media.__typename.equalsIgnoreCase("video")) {
                                    newComment.getAttachmentUrl().add(att.style_type_renderer.attachment.media.id);
                                }
                                if (att.style_type_renderer.attachment.media.__typename.equalsIgnoreCase("image")) {
                                    newComment.getAttachmentUrl().add(att.style_type_renderer.attachment.media.image.uri);
                                }
                            }
                        }
                        newComment.setCommentUrl(edge.node.feedback.url);
                        newComment.setOwningPostId(edge.node.parent_post_story.id);
                        newComment.setParentShareFbid(edge.node.parent_feedback.share_fbid);
                        newComment.setCommentReactionCount(edge.node.feedback.reactors.count);
                        newComment.setStartDate(commentsDTO.startDate);
                        try {
                            if (edge.node.created_time > 0) {
                                Date createDate = new Date((long) edge.node.created_time * 1000);
                                newComment.setCommentTime(f.format(createDate));
                            }
                        } catch (Exception e) {
                            System.out.println("Error parsing comment time");
                        }
                        comments.add(newComment);
                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Error while extracting comment");
                    }
                }
            }
            System.out.println("Finished extracting data from CommentsDTO");
        } catch (Exception e) {
            System.out.println("Error extracting data from CommentsDTO");
            e.printStackTrace();
        }
    }

    public void extractDataFromStoryCardDto(StoryCardDTO dto, ArrayList<ExtractedPosts> posts) {
        String postId = dto.data.story_card.post_id;
        ExtractedPosts post = posts.stream().filter(p -> p.getStoryId().equals(postId)).findFirst().orElse(null);
        boolean newBc = false;
        if(post == null){
            newBc = true;
            post = new ExtractedPosts();
            post.setStartDate(dto.startDate);
            post.setStoryId(postId);
            post.setStoryUri(dto.data.story_card.url);
        }

        try{
            ArrayList<StoryCardPack.Edge> comments = dto.data.feedback.ufi_renderer.feedback.comment_list_renderer.feedback.comment_rendering_instance_for_feed_location.comments.edges;
            for(StoryCardPack.Edge edge : comments){
                ExtractedComments newComment = new ExtractedComments();
                newComment.setCommentId(edge.node.id);
                newComment.setAuthorName(edge.node.author.name);
                newComment.setAuthorId(edge.node.author.id);
                newComment.setAuthorProfileUrl(edge.node.author.url);
                if(edge.node.body != null) {
                    newComment.setCommentText(edge.node.body.text);
                }
                if(edge.node.attachments != null) {
                    for(StoryCardPack.AttachmentComment att : edge.node.attachments) {
                        newComment.getAttachmentUrl().add(att.style_type_renderer.attachment.media.image.uri);
                    }
                }                newComment.setCommentUrl(edge.node.feedback.url);
                newComment.setOwningPostId(edge.node.parent_post_story.id);
                newComment.setParentShareFbid(edge.node.parent_feedback.share_fbid);
                newComment.setCommentReactionCount(edge.node.feedback.reactors.count);
                for(StoryCardPack.Edge commentEdge : edge.node.feedback.replies_connection.edges){
                    ExtractedComments replyComment = new ExtractedComments();
                    replyComment.setCommentId(commentEdge.node.id);
                    replyComment.setAuthorName(commentEdge.node.author.name);
                    replyComment.setAuthorId(commentEdge.node.author.id);
                    replyComment.setAuthorProfileUrl(commentEdge.node.author.url);
                    replyComment.setCommentText(commentEdge.node.body.text);
                    replyComment.setCommentUrl(commentEdge.node.feedback.url);
                    replyComment.setOwningPostId(commentEdge.node.parent_post_story.id);
                    replyComment.setParentShareFbid(commentEdge.node.parent_feedback.share_fbid);
                    replyComment.setCommentReactionCount(commentEdge.node.feedback.reactors.count);
                    newComment.getReplyComments().add(replyComment);
                }
                try {
                    if (edge.node.created_time > 0) {
                        Date createDate = new Date((long) edge.node.created_time * 1000);
                        newComment.setCommentTime(createDate.toString());
                    }
                } catch (Exception e){
                    System.out.println("Error parsing comment time");
                }
                post.getComments().add(newComment);
            }
        } catch (Exception e) {
            System.out.println("Error extracting comments from StoryCardDTO");
            e.printStackTrace();
        }
        if(newBc) {
            posts.add(post);
        }
    }

    public void extractDataFromVideoDto(VideoDTO dto, ArrayList<ExtractedPosts> posts) {
        try {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            System.out.println("Started extracting data from VideoDTO");
            CreationStory creationStory = dto.data.video.creation_story;
            ExtractedPosts newPost = posts.stream().filter(p -> p.getStoryId().equals(creationStory.post_id)).findFirst().orElse(null);
            boolean createdPost = false;
            if(newPost == null){
                newPost = new ExtractedPosts();
                newPost.setStoryId(creationStory.post_id);
                newPost.setStoryUri(creationStory.short_form_video_context.shareable_url);
                newPost.setId(creationStory.id);
                if(creationStory.message != null) {
                    newPost.setStoryDescription(creationStory.message.text);
                    newPost.setVideoComment(creationStory.message.text);
                }
                newPost.getVideosIds().add(creationStory.video.id);
                newPost.setStartDate(dto.startDate);
                newPost.setVideoOwnerId(creationStory.short_form_video_context.video_owner.id);
                createdPost = true;
            }

            if(createdPost) {
                posts.add(newPost);
            }

            System.out.println("Ended extracting data from TypenameUserTimelineFeedUnitsDTO");
        } catch (Exception e) {
            System.out.println("Error extracting data from TypenameUserTimelineFeedUnitsDTO");
            e.printStackTrace();
        }
    }

    public void extractDataFromTimelineFeedUnitsDto(TypenameUserTimelineFeedUnitsDTO dto, ArrayList<ExtractedPosts> posts) {
        // Extract data from TypenameUserTimelineFeedUnitsDTO
        try {
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            System.out.println("Started extracting data from TypenameUserTimelineFeedUnitsDTO");
            if(!dto.data.node.timeline_list_feed_units.edges.isEmpty()) {
            for(TypeNameUserPack.Edge edge : dto.data.node.timeline_list_feed_units.edges){
            ExtractedPosts newPost = posts.stream().filter(p -> p.getStoryId().equals(edge.node.post_id)).findFirst().orElse(null);
                    Node node = edge.node;
                    boolean createdPost = false;
                    if(newPost == null){
                        newPost = new ExtractedPosts();
                        newPost.setStoryId(node.post_id);
                        newPost.setId(dto.data.node.id);
                        createdPost = true;
                    }
                    newPost.setTypeName(node.__typename);
                    if (node.comet_sections.content.story.message != null) {
                        newPost.setStoryDescription((node.comet_sections.content.story.message.text));
                    }
                    //check same for videos
                    if (node.comet_sections.content.story.attachments.stream().anyMatch(a -> a.target.__typename.equalsIgnoreCase("photo"))) {
                        newPost.setHasPhotoAttachment(true);
                        ArrayList<String> photosIds = new ArrayList<>();
                        node.comet_sections.content.story.attachments.stream().filter(a -> a.target.__typename.equalsIgnoreCase("photo")).forEach(a -> photosIds.add(a.target.id));
                        newPost.setPhotosIds(photosIds);
                    }
                    if (node.comet_sections.content.story.attachments.stream().anyMatch(a -> a.target.__typename.equalsIgnoreCase("video"))) {
                        newPost.setHasVideoAttachment(true);
                        ArrayList<String> videosIds = new ArrayList<>();
                        node.comet_sections.content.story.attachments.stream().filter(a -> a.target.__typename.equalsIgnoreCase("video")).forEach(a -> videosIds.add(a.target.id));
                        newPost.setVideosIds(videosIds);
                    }

                    newPost.setStoryUri(node.comet_sections.content.story.wwwURL);
                    if(createdPost) {
                        posts.add(newPost);
                    }
                }
            }

            System.out.println("Ended extracting data from TypenameUserTimelineFeedUnitsDTO");
        } catch (Exception e) {
            System.out.println("Error extracting data from TypenameUserTimelineFeedUnitsDTO");
            e.printStackTrace();
        }
    }

    public void createExcelFile(List<ExtractedPosts> posts, Set<ExtractedCommentReactUsers> users) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet postsSheet = workbook.createSheet("PostsData");
            Row postsHeaderRow = postsSheet.createRow(0);
            for(int i = 0; i< Consts.postsFields.size(); i++){
                postsHeaderRow.createCell(i).setCellValue(Consts.postsFields.get(i));
            }
            int postIndex = 1;
            for(ExtractedPosts post : posts){
                Row dataRow = postsSheet.createRow(postIndex);
                dataRow.createCell(0).setCellValue(post.getGraphNodeId());
                dataRow.createCell(1).setCellValue(post.getId());
                dataRow.createCell(2).setCellValue(post.getStoryId());
                dataRow.createCell(3).setCellValue(post.getStoryUri());
                dataRow.createCell(4).setCellValue(post.getTypeName());
                dataRow.createCell(5).setCellValue(post.getStoryDescription());
                dataRow.createCell(6).setCellValue(post.getReactionCount() == null ? 0 : post.getReactionCount());
                dataRow.createCell(7).setCellValue(post.isHasVideoAttachment());
                dataRow.createCell(8).setCellValue(post.isHasPhotoAttachment());
                dataRow.createCell(9).setCellValue(post.getPhotosIds().toString());
                dataRow.createCell(10).setCellValue(post.getVideosIds().toString());
                dataRow.createCell(11).setCellValue(post.getVideoLink());
                dataRow.createCell(12).setCellValue(post.getVideoOwnerId());
                dataRow.createCell(13).setCellValue(post.getVideoComment());
                postIndex++;
            }

            for(int i = 0; i< Consts.postsFields.size(); i++){
                postsSheet.autoSizeColumn(i);
            }

            Sheet commetsSheet = workbook.createSheet("CommentsData");
            Row commentsHeaderRow = commetsSheet.createRow(0);
            for(int i = 0; i< Consts.commentsFields.size(); i++){
                commentsHeaderRow.createCell(i).setCellValue(Consts.commentsFields.get(i));
            }
            AtomicInteger commentIndex = new AtomicInteger(1);
            posts.stream().map(ExtractedPosts::getComments).forEach(c -> {
                for(ExtractedComments comment : c){
                    Row dataRow = commetsSheet.createRow(commentIndex.get());
                    dataRow.createCell(0).setCellValue(comment.getGraphNodeId());
                    dataRow.createCell(1).setCellValue(comment.getCommentId());
                    dataRow.createCell(2).setCellValue(comment.getAuthorName());
                    dataRow.createCell(3).setCellValue(comment.getAuthorId());
                    dataRow.createCell(4).setCellValue(comment.getAuthorProfileUrl());
                    dataRow.createCell(5).setCellValue(comment.getCommentText());
                    dataRow.createCell(6).setCellValue(comment.getAttachmentUrl().toString());
                    dataRow.createCell(7).setCellValue(comment.getCommentUrl());
                    dataRow.createCell(8).setCellValue(comment.getCommentReactionCount() == null ? 0 : comment.getCommentReactionCount());
                    dataRow.createCell(9).setCellValue(comment.getCommentTime());
                    dataRow.createCell(10).setCellValue(comment.getOwningPostId());
                    dataRow.createCell(11).setCellValue(comment.getParentShareFbid());
                    dataRow.createCell(12).setCellValue(comment.getReplyComments().toString());
                    commentIndex.getAndIncrement();
                }
            });

            for(int i = 0; i< Consts.commentsFields.size(); i++){
                commetsSheet.autoSizeColumn(i);
            }


            Sheet reactionsSheet = workbook.createSheet("ReactionsData");
            Row reactionHeaderRow = reactionsSheet.createRow(0);
            for(int i = 0; i< Consts.reactionsFields.size(); i++){
                reactionHeaderRow.createCell(i).setCellValue(Consts.reactionsFields.get(i));
            }
            AtomicInteger reactionsIndex = new AtomicInteger(1);
            posts.stream().map(ExtractedPosts::getReactions).forEach(c -> {
                for(ExtractedReactions reaction : c){
                    Row dataRow = reactionsSheet.createRow(reactionsIndex.get());
                    dataRow.createCell(0).setCellValue(reaction.getGraphNodeId());
                    dataRow.createCell(1).setCellValue(reaction.getReactionId());
                    dataRow.createCell(2).setCellValue(reaction.getAuthorName());
                    dataRow.createCell(3).setCellValue(reaction.getAuthorProfileUrl());
                    dataRow.createCell(4).setCellValue(reaction.getReactionImageUrl());
                    reactionsIndex.getAndIncrement();
                }
            });

            for(int i = 0; i< Consts.reactionsFields.size(); i++){
                reactionsSheet.autoSizeColumn(i);
            }

            Sheet usersSheet = workbook.createSheet("UsersData");
            Row usersHeaderRow = usersSheet.createRow(0);
            for(int i = 0; i< Consts.usersFields.size(); i++){
                usersHeaderRow.createCell(i).setCellValue(Consts.usersFields.get(i));
            }
            AtomicInteger userIndex = new AtomicInteger(1);
            users.forEach(u -> {
                Row dataRow = usersSheet.createRow(userIndex.get());
                dataRow.createCell(0).setCellValue(u.getGraphNodeId());
                dataRow.createCell(1).setCellValue(u.getAuthorId());
                dataRow.createCell(2).setCellValue(u.getAuthorName());
                dataRow.createCell(3).setCellValue(u.getAuthorProfileUrl());
                userIndex.getAndIncrement();
            });

            for(int i = 0; i< Consts.usersFields.size(); i++){
                usersSheet.autoSizeColumn(i);
            }

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String date = format.format( Date.from(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC)));
            try (FileOutputStream fileOut = new FileOutputStream("src/main/resources/Exports/ExportedExcel" + date + ".xlsx")) {
                workbook.write(fileOut);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Excel file created successfully.");
        } catch (Exception e) {
            System.out.println("Error creating Excel file.");
            e.printStackTrace();
        }
    }

    public void extractDataFromHarFile(List<HarEntry> allHarEntries, ObjectMapper objectMapper, List<StoryCardDTO> storyCardDTOList, List<TypenameUserTimelineFeedUnitsDTO> timelineFeedUnitsDtoList, List<TypenameStoryDTO> typenameStoryDTOList, List<VideoDTO> videoDTOList, List<CommentsDTO> commentsDTOList, List<ReactionsDTO> reactionsDTOList) {
        //This har has info about posts of feed, how many posts there are in the feed and basic data
        List<HarEntry> timelineFeedUnitsList = allHarEntries.stream().filter(entry-> entry.getRequest().getUrl().equalsIgnoreCase("https://www.facebook.com/api/graphql/")).filter(a->a.getResponse().getContent().getText().length() > 500)
                .filter(a-> a.getResponse().getContent().getText().contains("{\"data\":{\"node\":{\"__typename\":\"User\",\"id\":") &&
                        a.getResponse().getContent().getText().substring(0,200).contains("timeline_list_feed_units")).collect(Collectors.toList());

        //basic info for each story
        List<HarEntry> typeNameStoryList = allHarEntries.stream().filter(entry-> entry.getRequest().getUrl().equalsIgnoreCase("https://www.facebook.com/api/graphql/")).filter(a->a.getResponse().getContent().getText().length() > 500)
                .filter(a-> a.getResponse().getContent().getText().contains("{\"data\":{\"node\":{\"__typename\":\"Story\",")).collect(Collectors.toList());

        //info when you click on a story and window pops up
        List<HarEntry> storyCardList = allHarEntries.stream().filter(entry-> entry.getRequest().getUrl().equalsIgnoreCase("https://www.facebook.com/api/graphql/")).filter(a->a.getResponse().getContent().getText().length() > 500)
                .filter(a-> a.getResponse().getContent().getText().contains("{\"data\":{\"story_card\":{\"is_text_only_story")).collect(Collectors.toList());

        //videos
        List<HarEntry> videosList = allHarEntries.stream().filter(entry-> entry.getRequest().getUrl().equalsIgnoreCase("https://www.facebook.com/api/graphql/")).filter(a->a.getResponse().getContent().getText().length() > 500)
                .filter(a-> a.getResponse().getContent().getText().contains("{\"data\":{\"video\":{\"creation_story\":")).collect(Collectors.toList());

        //comments
        List<HarEntry> commentsList = allHarEntries.stream().filter(entry-> entry.getRequest().getUrl().equalsIgnoreCase("https://www.facebook.com/api/graphql/")).filter(a->a.getResponse().getContent().getText().length() > 500)
                .filter(a-> a.getResponse().getContent().getText().contains("{\"data\":{\"node\":{\"__typename\":\"Feedback\",\"comment_rendering_instance_for_feed_location")).collect(Collectors.toList());

        //reactions
        List<HarEntry> reactorsList = allHarEntries.stream().filter(entry-> entry.getRequest().getUrl().equalsIgnoreCase("https://www.facebook.com/api/graphql/")).filter(a->a.getResponse().getContent().getText().length() > 500)
                .filter(a-> a.getResponse().getContent().getText().contains("{\"data\":{\"node\":{\"__typename\":\"Feedback\",\"reactors\":{\"edges\":[{\"feedback_reaction_info") ||
                        a.getResponse().getContent().getText().contains("{\"data\":{\"node\":{\"__typename\":\"Feedback\",\"associated_group\":")).collect(Collectors.toList());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setStrictness(Strictness.LENIENT);
        Gson gson = gsonBuilder.setPrettyPrinting().create();


        storyCardList.forEach(entry -> {
            String prettyJson = this.parseResponseToJson(entry, objectMapper);
            JsonElement jsonElement = gson.fromJson(prettyJson, JsonElement.class);
            StoryCardDTO item =  gson.fromJson(jsonElement, StoryCardDTO.class);
            item.startDate = entry.getStartedDateTime();
            storyCardDTOList.add(item);
        });

        timelineFeedUnitsList.forEach(entry -> {
            String prettyJson = this.parseResponseToJson(entry, objectMapper);
            JsonElement jsonElement = gson.fromJson(prettyJson, JsonElement.class);
            TypenameUserTimelineFeedUnitsDTO item =  gson.fromJson(jsonElement, TypenameUserTimelineFeedUnitsDTO.class);
            timelineFeedUnitsDtoList.add(item);
        });

        typeNameStoryList.forEach(entry -> {
            String prettyJson = this.parseResponseToJson(entry, objectMapper);
            JsonElement jsonElement = gson.fromJson(prettyJson, JsonElement.class);
            TypenameStoryDTO item =  gson.fromJson(jsonElement, TypenameStoryDTO.class);
            typenameStoryDTOList.add(item);
        });

        videosList.forEach(entry -> {
            String prettyJson = this.parseResponseToJson(entry, objectMapper);
            JsonElement jsonElement = gson.fromJson(prettyJson, JsonElement.class);
            VideoDTO item =  gson.fromJson(jsonElement, VideoDTO.class);
            item.startDate = entry.getStartedDateTime();
            videoDTOList.add(item);
        });

        commentsList.forEach(entry -> {
            String prettyJson = this.parseResponseToJson(entry, objectMapper);
            JsonElement jsonElement = gson.fromJson(prettyJson, JsonElement.class);
            CommentsDTO item =  gson.fromJson(jsonElement, CommentsDTO.class);
            item.startDate = entry.getStartedDateTime();
            commentsDTOList.add(item);
        });

        reactorsList.forEach(entry -> {
            String prettyJson = this.parseResponseToJson(entry, objectMapper);
            JsonElement jsonElement = gson.fromJson(prettyJson, JsonElement.class);
            ReactionsDTO item =  gson.fromJson(jsonElement, ReactionsDTO.class);
            item.startDate = entry.getStartedDateTime();
            reactionsDTOList.add(item);
        });
    }

    public void extractPostsLikesCommentsUsers(List<StoryCardDTO> storyCardDTOList, ArrayList<ExtractedPosts> extractedPosts, List<TypenameUserTimelineFeedUnitsDTO> timelineFeedUnitsDtoList, List<VideoDTO> videoDTOList, List<CommentsDTO> commentsDTOList, List<ReactionsDTO> reactionsDTOList, Set<ExtractedCommentReactUsers> users) {
        storyCardDTOList.forEach(storyCard -> {
            this.extractDataFromStoryCardDto(storyCard, extractedPosts);
        });

        timelineFeedUnitsDtoList.forEach(tfu -> {
            this.extractDataFromTimelineFeedUnitsDto(tfu, extractedPosts);
        });

        videoDTOList.forEach(video -> {
            this.extractDataFromVideoDto(video, extractedPosts);
        });

        ArrayList<ExtractedComments> extractedComments = new ArrayList<>();
        commentsDTOList.forEach(comments -> {
            this.extractDataFromCommentsDto(comments, extractedComments);
        });

        ArrayList<ExtractedReactions> extractedReactions = new ArrayList<>();
        reactionsDTOList.forEach(reactions -> {
            this.extractDataFromReactionsDTP(reactions, extractedReactions);
        });
        extractedPosts.sort(Comparator.comparing(ExtractedPosts::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
        int nullDatePos = 0;
        for(int i = 0; i < extractedPosts.size(); i++){
            ExtractedPosts post = extractedPosts.get(i);
            if(post.getStartDate() == null){
                nullDatePos = i;
                break;
            }
            if(i == extractedPosts.size() - 1){
                nullDatePos = extractedPosts.size();
            }
        }
        for(int i = 0; i < nullDatePos; i++){
            Date actualPostDate = extractedPosts.get(i).getStartDate();

            if(i + 1 < nullDatePos){
                Date nextPostDate = extractedPosts.get(i + 1).getStartDate();
                List<ExtractedComments> commentsForPost = extractedComments.stream().filter(a-> a.getStartDate().after(actualPostDate) && a.getStartDate().before(nextPostDate)).collect(Collectors.toList());
                List<ExtractedReactions> reactionsForPost = extractedReactions.stream().filter(a-> a.getStartDate().after(actualPostDate) && a.getStartDate().before(nextPostDate)).collect(Collectors.toList());
                extractedPosts.get(i).getComments().addAll(commentsForPost);
                extractedPosts.get(i).getReactions().addAll(reactionsForPost);
            }
            if(i + 1 == nullDatePos){
                List<ExtractedComments> commentsForPost = extractedComments.stream().filter(a-> a.getStartDate().after(actualPostDate)).collect(Collectors.toList());
                List<ExtractedReactions> reactionsForPost = extractedReactions.stream().filter(a-> a.getStartDate().after(actualPostDate)).collect(Collectors.toList());
                extractedPosts.get(i).getComments().addAll(commentsForPost);
                extractedPosts.get(i).getReactions().addAll(reactionsForPost);
            }
        }
        AtomicInteger counter = new AtomicInteger(1);
        extractedPosts.forEach(post -> {
            post.setGraphNodeId(String.valueOf(counter.getAndIncrement()));
        });
        counter.set(1);
        extractedPosts.stream().flatMap(ep -> ep.getComments().stream()).forEach(comment -> {
            comment.setGraphNodeId(String.valueOf(counter.getAndIncrement()));
            comment.getReplyComments().forEach(replyComment -> {
                replyComment.setGraphNodeId(String.valueOf(counter.getAndIncrement()));
            });
        });
        counter.set(1);
        extractedPosts.stream().flatMap(ep -> ep.getReactions().stream()).forEach(reaction -> {
            reaction.setGraphNodeId(String.valueOf(counter.getAndIncrement()));
        });
        counter.set(1);
        extractedPosts.stream().flatMap(ep -> ep.getComments().stream()).forEach(comment -> {
            users.add(new ExtractedCommentReactUsers(String.valueOf(users.size() + 1), comment.getAuthorName() ,comment.getAuthorId(), comment.getAuthorProfileUrl()));
        });
        extractedPosts.stream().flatMap(ep -> ep.getReactions().stream()).forEach(reaction -> {
            if(users.stream().noneMatch(u -> u.getAuthorProfileUrl().equals(reaction.getAuthorProfileUrl()))){
                users.add(new ExtractedCommentReactUsers(String.valueOf(users.size() + 1), reaction.getAuthorName() ,"", reaction.getAuthorProfileUrl()));
            }
        });
    }

    public boolean checkIfCoCommented(ExtractedCommentReactUsers user, ExtractedCommentReactUsers user1) {
        for(Integer postId : user.getListOfPostsCommentedTo()){
            if(user1.getListOfPostsCommentedTo().contains(postId)){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfCoReacted(ExtractedCommentReactUsers user, ExtractedCommentReactUsers user1) {
        for(Integer postId : user.getListOfPostsReactedTo()){
            if(user1.getListOfPostsReactedTo().contains(postId)){
                return true;
            }
        }
        return false;
    }
}
