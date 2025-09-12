package org.example;

import CommentPack.CommentsDTO;
import ExtractedData.CoCommentCoReactUsers;
import ExtractedData.ExtractedCommentReactUsers;
import ExtractedData.ExtractedPosts;
import ReactionPack.ReactionsDTO;
import StoryCardPack.StoryCardDTO;
import TypeNameStoryPack.TypenameStoryDTO;
import TypeNameUserPack.TypenameUserTimelineFeedUnitsDTO;
import VideoPack.VideoDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderMode;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        System.setProperty("org.graphstream.ui", "swing");
        Methods methods = new Methods();
        GraphMethods graphMethods = new GraphMethods();

        try {
            HarReader harReader = new HarReader();
            Har har = harReader.readFromFile(new File("src/main/resources/source.har"), HarReaderMode.LAX);
            System.out.println(har.getLog().getCreator().getName());
            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


            List<StoryCardDTO> storyCardDTOList = new ArrayList<>();
            List<TypenameUserTimelineFeedUnitsDTO> timelineFeedUnitsDtoList = new ArrayList<>();
            List<TypenameStoryDTO> typenameStoryDTOList = new ArrayList<>();
            List<VideoDTO> videoDTOList = new ArrayList<>();
            List<CommentsDTO> commentsDTOList = new ArrayList<>();
            List<ReactionsDTO> reactionsDTOList = new ArrayList<>();
            
//          Extract data from har file
            List<HarEntry> allHarEntries = har.getLog().getEntries();
            methods.extractDataFromHarFile(allHarEntries, objectMapper, storyCardDTOList, timelineFeedUnitsDtoList, typenameStoryDTOList, videoDTOList, commentsDTOList, reactionsDTOList);


//          Extract posts, comments, reactions and users
            ArrayList<ExtractedPosts> extractedPosts = new ArrayList<>();

            Set<ExtractedCommentReactUsers> users = new HashSet<>();
            methods.extractPostsLikesCommentsUsers(storyCardDTOList, extractedPosts, timelineFeedUnitsDtoList, videoDTOList, commentsDTOList, reactionsDTOList, users);
            methods.createExcelFile(extractedPosts, users);


            Graph graph = new DefaultGraph("1");
            extractedPosts.forEach(post -> {
                graphMethods.createPostNode(graph, post);
            });
            extractedPosts.stream().flatMap(ep -> ep.getComments().stream()).forEach(comment -> {
                graphMethods.createCommentNode(graph, comment);
            });
            extractedPosts.stream().flatMap(ep -> ep.getComments().stream().flatMap(c -> c.getReplyComments().stream())).forEach(replyComment -> {
                graphMethods.createCommentNode(graph, replyComment);
            });
            extractedPosts.stream().flatMap(ep -> ep.getReactions().stream()).forEach(reaction -> {
                graphMethods.createReactionNode(graph, reaction);
            });
            users.forEach(user -> {
                graphMethods.createUserNode(graph, user);
            });
            extractedPosts.forEach(post -> {
                graphMethods.createAllEdgesForPost(graph, post, users);
            });

            List<CoCommentCoReactUsers> coCommentUsers = new ArrayList<>();
            List<CoCommentCoReactUsers> coReactUsersUsers = new ArrayList<>();

            Graph coActionUserGraph = new MultiGraph("2");
            users.forEach(user -> {
                graphMethods.createUserNode(coActionUserGraph, user);
            });

            int[][] coCommentMatrix = new int[extractedPosts.size()][users.size()];
            int[][] coReactMatrix = new int[extractedPosts.size()][users.size()];
            extractedPosts.forEach(p ->{
                p.getComments().forEach(c -> {
                    ExtractedCommentReactUsers user = users.stream().filter(u -> u.getAuthorId().equals(c.getAuthorId())).findFirst().get();
                    coCommentMatrix[Integer.valueOf(p.getGraphNodeId()) - 1][Integer.valueOf(user.getGraphNodeId()) - 1] = 1;
                });
                p.getReactions().forEach(r -> {
                    ExtractedCommentReactUsers user = users.stream().filter(u -> u.getAuthorProfileUrl().equals(r.getAuthorProfileUrl())).findFirst().get();
                    coReactMatrix[Integer.valueOf(p.getGraphNodeId()) - 1][Integer.valueOf(user.getGraphNodeId()) - 1] = 1;
                });
            });

            int lowerLimitToReactsCount = 2;

            for(int i = 0 ; i < users.size(); i++){
                List<Integer> postsCommentedOn = new ArrayList<>();
                for(int j = 0; j < extractedPosts.size(); j++){
                    if(coCommentMatrix[j][i] == 1){
                        postsCommentedOn.add(j + 1);
                    }
                }
                int ii = i + 1;
                users.stream().filter(u -> u.getGraphNodeId().equals(String.valueOf(ii))).findFirst().get().setListOfPostsCommentedTo(postsCommentedOn);
            }

            for(int i = 0 ; i < users.size(); i++){
                List<Integer> postsReactedOn = new ArrayList<>();
                for(int j = 0; j < extractedPosts.size(); j++){
                    if(coReactMatrix[j][j] == 1){
                        postsReactedOn.add(j + 1);
                    }
                }
                int ii = i + 1;
                users.stream().filter(u -> u.getGraphNodeId().equals(String.valueOf(ii))).findFirst().get().setListOfPostsReactedTo(postsReactedOn);
            }

            for (ExtractedCommentReactUsers user : users) {
                for (ExtractedCommentReactUsers user1 : users) {
                    if (!user.equals(user1)) {
                        if (user.getListOfPostsCommentedTo().size() >= lowerLimitToReactsCount && user1.getListOfPostsCommentedTo().size() >= lowerLimitToReactsCount) {
                            if (methods.checkIfCoCommented(user, user1)) {
                                coCommentUsers.add(new CoCommentCoReactUsers(user, user1));
                            }
                        }
                        if (user.getListOfPostsReactedTo().size() >= lowerLimitToReactsCount && user1.getListOfPostsReactedTo().size() >= lowerLimitToReactsCount) {
                            if (methods.checkIfCoReacted(user, user1)) {
                                coReactUsersUsers.add(new CoCommentCoReactUsers(user, user1));
                            }
                        }
                    }
                }
            }
            List<String> createdCommentEdges = new ArrayList<>();
            List<String> createdReactionEdges = new ArrayList<>();
            graphMethods.createEdgesForUserUserCoActionGraph(coActionUserGraph, coCommentUsers, true, createdCommentEdges);
            graphMethods.createEdgesForUserUserCoActionGraph(coActionUserGraph, coReactUsersUsers, false, createdReactionEdges);

//          Display graph, save graphml file
            Viewer viewer1 = graph.display();
            Viewer viewer = coActionUserGraph.display();
            graphMethods.createAndSaveGraphMlFile(graph, false);
            graphMethods.createAndSaveGraphMlFile(coActionUserGraph, true);

            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}