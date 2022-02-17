package Twitter.HW2;

import Twitter.Tweet;
import Twitter.TwitterAPI;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

/**
 * Implements the TwitterAPI Interface using a connection to Redis, implementing Strategy 2 from the HW Doc
 *
 * The Redis schema contains tweet objects represented as tweet:{tweed_Id} as well as a latestTweetID
 * which is the id of the latest tweet in the system. It also contains a set of users in the system, including
 * users who have tweeted and users who follow or are followed by users who have tweeted. We store a 
 * latestUserID which is the id of the latest user in the system. Users are represented as user:{user_Id} with
 * user_Id being the id of the user. Their followers are represented as followers:{user_Id} (a list of people
 * who follow the user) and their timeline is represented as timeline:{user_Id} (a list of tweets posted by this
 * user's follows).
 */
public class TwitterHW2Imp implements TwitterAPI {
    private Jedis jedis;
    private int latestTweetID;

    public TwitterHW2Imp() {
        this.jedis = new Jedis();
        this.latestTweetID = 1;
        jedis.flushAll();
    }

    @Override
    public void postTweet(Tweet t) {
        this.jedis.hset("tweet:" + (latestTweetID + 1), "user_id", t.getUserId()); // add tweet to database with the latest tweet id

        Set<Integer> followers = this.getFollowers(t.getUserId()); // get followers of the user who posted this tweet
        
        // for each follower, add tweet id to their timeline
        for (Integer follower : followers) {
            this.jedis.lpush("timeline:" + follower, latestTweetID);
        }

        latestTweetID++; // increment latest tweet id
    }

    @Override
    private List<Integer> getTimeline(int userId) {
        // get the last 10 tweets from the timeline of the user with the given user id
        List<String> fullTimelineString = this.jedis.hget("user:" + userId, "timeline");
        // get the last 10 tweets from the timeline
        List<String> timelineString = fullTimelineString.subList(Math.max(0, fullTimelineString.size() - 10), fullTimelineString.size());

        // convert the timeline to a list of tweet ids
        List<Integer> timeline = new ArrayList<>();
        for (String tweetId : timelineString) {
            try {
                timeline.add(Integer.parseInt(tweetId));
            } catch (NumberFormatException e) {
                System.out.println("Error parsing tweet id: " + tweetId);
            }
        }

        return timeline;
    }

    @Override
    public Set<Integer> getFollowers(int userId) {
        List<String> followers = this.jedis.hget("user:" + userId, "followers");
        List<Integer> followerIds = new ArrayList<>();

        for (String follower : followers) {
            try {
                followerIds.add(Integer.parseInt(follower));
            } catch (NumberFormatException e) {
                System.out.println("Error parsing follower: " + follower);
            }
        }

        return followerIds;
    }

    @Override
    public Set<Integer> getFollowees(int userId) {
        throw new UnsupportedOperationException("We are not doing this for this hw lol");
    }

    @Override
    public List<Tweet> getTweets(int userId) {
        throw new UnsupportedOperationException("We are not doing this for this hw lol");
    }

    @Override
    public List<Integer> getAllUsers() {
        List<String> userIdStrings = this.jedis.lrange("users", 0, -1);
        List<Integer> userIds = new java.util.ArrayList<>();

        for (String userIdString : userIdStrings) {
            try {
                userIds.add(Integer.parseInt(userIdString));
            } catch (NumberFormatException e) {
                System.out.println("Error parsing userId: " + userIdString);
            }
        }

        return userIds;
    }

    @Override
    public void addFollow(int userId, int followId) {
        // if the user does not already exist, create a followers, follows list, and timeline for them.
        // if the follow does not already exist, create a followers, follows list, and timeline for them.
        // add the user to the list of users
        // add the userId to the followId's followers
        // add the followID to the userId's follows (**NECESSARY FOR GET TIMELINE**)
        if (!this.jedis.exists("user:" + userId)) {
            this.jedis.hset("user:" + userId, "followers", "followers:" + userId);
            this.jedis.hset("user:" + userId, "follows", "follows:" + userId);
            this.jedis.hset("user:" + userId, "timeline", "timeline:" + userId);
            this.jedis.lpush("users", userId);
        }
        if (!this.jedis.exists("user:" + followId)) {
            this.jedis.hset("user:" + followId, "follows", "follows:" + followId);
            this.jedis.hset("user:" + followId, "followers", "followers:" + followId);
            this.jedis.hset("user:" + followId, "timeline", "timeline:" + followId);
            this.jedis.lpush("users", followId);
        }
        this.jedis.sadd("followers:" + followId, userId);
        this.jedis.sadd("follows:" + userId, followId);
    }

    @Override
    public void closeConnection() {
        this.jedis.close();
    }
}
