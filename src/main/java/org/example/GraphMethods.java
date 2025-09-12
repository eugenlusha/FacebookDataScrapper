package org.example;

import ExtractedData.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkGraphML;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class GraphMethods {

    public void createPostNode(Graph graph, ExtractedPosts post){
        Node node = graph.addNode("postId:" + post.getGraphNodeId());
        node.setAttribute("ui.style", "shape: diamond; fill-color: blue; size: 5px; text-size: 10px; text-color: black; text-alignment: at-right;");
        node.setAttribute("ui.label", "PostId : " + post.getGraphNodeId());
        node.setAttribute("StoryId", post.getStoryId() == null ? "" : post.getStoryId());
        node.setAttribute("StoryUri", post.getStoryUri() == null ? "" : post.getStoryUri());
        node.setAttribute("TypeName", post.getTypeName() == null ? "" : post.getTypeName());
        node.setAttribute("StoryDescription", post.getStoryDescription() == null ? "" : post.getStoryDescription());
        node.setAttribute("ReactionCount", post.getReactionCount() == null ? 0 : post.getReactionCount());
        node.setAttribute("HasVideoAttachment", post.isHasVideoAttachment() );
        node.setAttribute("HasPhotoAttachment", post.isHasPhotoAttachment());
        node.setAttribute("PhotosIds", post.getPhotosIds() == null ? "" : post.getPhotosIds());
        node.setAttribute("VideosIds", post.getVideosIds() == null ? "" : post.getVideosIds());
        node.setAttribute("VideoLink", post.getVideoLink() == null ? "" : post.getVideoLink());
        node.setAttribute("VideoOwnerId", post.getVideoOwnerId() == null ? "" : post.getVideoOwnerId());
        node.setAttribute("VideoComment", post.getVideoComment() == null ? "" : post.getVideoComment());
    }

    public void createCommentNode(Graph graph, ExtractedComments comment) {
        Node node = graph.addNode("commentId:" +comment.getGraphNodeId());
        node.setAttribute("ui.label", "CommentId : " + comment.getGraphNodeId());
        node.setAttribute("ui.style", "shape: circle; fill-color: yellow; size: 5px; text-size: 10px; text-color: black; text-alignment: at-right;");
        node.setAttribute("CommentId", comment.getCommentId() == null ? "" : comment.getCommentId());
        node.setAttribute("CommentText", comment.getCommentText() == null ? "" : comment.getCommentText());
        node.setAttribute("AttachmentUrl", comment.getAttachmentUrl() == null ? "" : comment.getAttachmentUrl());
        node.setAttribute("CommentUrl", comment.getCommentUrl() == null ? "" : comment.getCommentUrl());
        node.setAttribute("CommentReactionCount", comment.getCommentReactionCount() == null ? 0 : comment.getCommentReactionCount());
        node.setAttribute("CommentTime", comment.getCommentTime() == null ? "" : comment.getCommentTime());
    }

    public void createReactionNode(Graph graph, ExtractedReactions reaction) {
        Node node = graph.addNode("reactionId:" + reaction.getGraphNodeId());
        node.setAttribute("ui.label", "ReactionId : " + reaction.getGraphNodeId());
        node.setAttribute("ui.style", "shape: circle; fill-color: red; size: 5px; text-size: 10px; text-color: black; text-alignment: at-right;");
        node.setAttribute("ReactionId", reaction.getReactionId() == null ? "" : reaction.getReactionId());
        node.setAttribute("ReactionImageUrl", reaction.getReactionImageUrl() == null ? "" : reaction.getReactionImageUrl());
    }

    public void createUserNode(Graph graph, ExtractedCommentReactUsers user) {
        Node node = graph.addNode("userId:" + user.getGraphNodeId());
        node.setAttribute("ui.style", "shape: box; fill-color: green; size: 5px; text-size: 10px; text-color: black; text-alignment: at-right;");
        node.setAttribute("ui.label", "UserId : " + user.getGraphNodeId());
        node.setAttribute("UserId", user.getAuthorId());
        node.setAttribute("UserName", user.getAuthorName());
        node.setAttribute("UserUrl", user.getAuthorProfileUrl());
    }

    public void createAllEdgesForPost(Graph graph, ExtractedPosts post, Set<ExtractedCommentReactUsers> users) {
        post.getComments().forEach(c -> {
            String commentinUserId = users.stream().filter(u -> u.getAuthorId().equals(c.getAuthorId())).findFirst().get().getGraphNodeId();
            this.createEdge(graph, "userId:" + commentinUserId, "commentId:" + c.getGraphNodeId(), "userId:" + commentinUserId + "- Commented - " + "commentId:" + c.getGraphNodeId());
            this.createEdge(graph, "commentId:" + c.getGraphNodeId(), "postId:" + post.getGraphNodeId(), "commentId:" + c.getGraphNodeId() + "- CommentedOn - " + "postId:" + post.getGraphNodeId());
            c.getReplyComments().forEach(rc -> {
                String replyCommentingUserId = users.stream().filter(u -> u.getAuthorId().equals(rc.getAuthorId())).findFirst().get().getGraphNodeId();
                this.createEdge(graph, "userId:" + replyCommentingUserId, "commentId:" + rc.getGraphNodeId(), "userId:" + replyCommentingUserId + "- Commented - " + "commentId:" + rc.getGraphNodeId());
                this.createEdge(graph, "commentId:" + rc.getGraphNodeId(), "commentId:" + c.getGraphNodeId(), "commentId:" + rc.getGraphNodeId() + "- CommentedOn - " + "commentId:" + c.getGraphNodeId());
            });
        });
        post.getReactions().forEach(r -> {
            String reactioninUserId = users.stream().filter(u -> u.getAuthorProfileUrl().equals(r.getAuthorProfileUrl())).findFirst().get().getGraphNodeId();
            this.createEdge(graph, "userId:" + reactioninUserId, "reactionId:" + r.getGraphNodeId(), "userId:" + reactioninUserId + "- Reacted - " + "reactionId:" + r.getGraphNodeId());
            this.createEdge(graph, "reactionId:" + r.getGraphNodeId(), "postId:" + post.getGraphNodeId(), "reactionId:" + r.getGraphNodeId() + "- ReactedOn - " + "postId:" + post.getGraphNodeId());
        });
    }

    public void createEdgesForUserUserCoActionGraph(Graph graph, List<CoCommentCoReactUsers> coCommentCoReactUsers, boolean isCoCommentList, List<String> createdEdges) {
        try{

            String action;
            if(!isCoCommentList){
                action = "CoReaction";
            } else {
                action = "CoComment";
            }

            coCommentCoReactUsers.forEach(coCommentCoReactUser -> {
                String originNode = "userId:" + coCommentCoReactUser.getUser1().getGraphNodeId();
                String destNode = "userId:" + coCommentCoReactUser.getUser2().getGraphNodeId();
                String key1 = originNode + "-" + destNode;
                String key2 = destNode + "-" + originNode;
                if(!createdEdges.contains(key1) && !createdEdges.contains(key2)){
                    this.createEdgeWithoutDirection(graph, originNode, destNode, "userId:" + coCommentCoReactUser.getUser1().getGraphNodeId() + "- "+ action + " - " + "userId:" + coCommentCoReactUser.getUser2().getGraphNodeId());
                    createdEdges.add(key1);
                }
            });
        } catch (EdgeRejectedException e) {
            e.printStackTrace();
        }
    }

    public void createEdge(Graph graph, String originNode, String destNode, String action) {
           Edge edge = graph.addEdge(action, originNode, destNode);
           edge.setAttribute("ui.style", "text-size: 10px; text-color: purple; text-alignment: at-right;");

    }public void createEdgeWithoutDirection(Graph graph, String originNode, String destNode, String action) {
           Edge edge = graph.addEdge(action, originNode, destNode);
           edge.setAttribute("ui.style", "text-size: 10px; text-color: purple; text-alignment: at-right;", false);
    }

    public void createAndSaveGraphMlFile(Graph graph, boolean isCoActionGraph) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String date = format.format( Date.from(LocalDateTime.now().toInstant(java.time.ZoneOffset.UTC)));

        // Create a GraphMLWriter instance
        FileSinkGraphML writer = new FileSinkGraphML ();
        String graphTypeName = "FullGraph";
        if(isCoActionGraph) {
            graphTypeName = "CoActionGraph";
        }
        try {
            // Specify the file name for the output GraphML file
            FileWriter fileWriter = new FileWriter("src/main/resources/Exports/"+ graphTypeName + date + ".graphml");
            // Write the graph to the GraphML file
            writer.writeAll(graph, fileWriter);
            // Close the file writer
            fileWriter.close();
            // Print success message
            System.out.println("Graph has been exported to output.graphml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
