package Twitter.HW2;

import Twitter.Tweet;
import Twitter.TwitterAPI;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements the TwitterAPI Interface using a connection to Redis, implementing Strategy 2 from the HW Doc
 * <p>
 * The Redis schema contains tweet objects represented as tweet:{tweed_Id} as well as a latestTweetID
 * which is the id of the latest tweet in the system. It also contains a set of users in the system, including
 * users who have tweeted and users who follow or are followed by users who have tweeted. We store a
 * latestUserID which is the id of the latest user in the system. Users are represented as user:{user_Id} with
 * user_Id being the id of the user. Their followers are represented as followers:{user_Id} (a list of people
 * who follow the user) and their timeline is represented as timeline:{user_Id} (a list of tweets posted by this
 * user's follows).
 */
public class TwitterHW2Imp implements TwitterAPI {
    private final Jedis jedis;

    public static String LATEST_TWEET_ID_KEY = "latestTweetId";
    public static String FOLLOWERS_PREFIX = "followers:";
    public static String FOLLOWS_PREFIX = "follows:";
    public static String TIMELINE_PREFIX = "timeline:";
    public static String TWEET_HASH_KEY = "tweet";

    public TwitterHW2Imp() {
        this.jedis = new Jedis();
        jedis.set("latestTweetId", "1");
    }

    /**
     * Constructs an instance of the TwitterHW2Imp, with the given option to flush what is currently in the db.
     *
     * @param flush - a boolean representing if we should flush the db or not. Flush if true, else do nothing.
     */
    public TwitterHW2Imp(boolean flush) {
        this();
        if (flush) {
            jedis.flushAll();
        }
    }


    @Override
    public void postTweet(Tweet t) {
        String latestTweetId = getLatestTweetId();
        // add tweet to database with the latest tweet id in the form userId tweetTxt tweetTS
        this.jedis.hset(TWEET_HASH_KEY, latestTweetId, t.toString());
        Set<Integer> followers = this.getFollowers(t.getUserId()); // get followers of the user who posted this tweet
        // for each follower, add tweet id to their timeline
        for (Integer follower : followers) {
            this.jedis.lpush(TIMELINE_PREFIX + follower, latestTweetId);
        }
        incrementLatestTweetId();
    }

    @Override
    public List<Tweet> getTimeline(int userId) {
        // get latest 10 tweetIDs from jedis, and convert them to ints
        List<Integer> tweetIds = jedis.lrange(TIMELINE_PREFIX + userId, 0, 10).stream()
                .map(Integer::parseInt).collect(Collectors.toList());

        // for each tweet, get the tweet hash info, parse it, and convert it to a Tweet object
        return tweetIds.stream().map(id -> {
            String tweetInfo = jedis.hget(TWEET_HASH_KEY, Integer.toString(userId));
            List<String> seperatedTweetInfo = Arrays.asList(tweetInfo.split(" "));
            return new Tweet(id, userId, seperatedTweetInfo.get(0), Timestamp.valueOf(seperatedTweetInfo.get(1)));
        }).collect(Collectors.toList());
    }

    @Override
    public Set<Integer> getFollowers(int userId) {
        Set<String> followers = jedis.smembers(FOLLOWERS_PREFIX + userId);
        return followers.stream().map(Integer::parseInt).collect(Collectors.toSet());
    }

    @Override
    public Set<Integer> getFollowees(int userId) {
        Set<String> follows = jedis.smembers(FOLLOWS_PREFIX + userId);
        return follows.stream().map(Integer::parseInt).collect(Collectors.toSet());
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
        // add the userId to the followId's followers
        jedis.sadd(FOLLOWERS_PREFIX + followId, Integer.toString(userId));

        // add the followID to the userId's follows (**NECESSARY FOR GET TIMELINE**)
        jedis.sadd(FOLLOWS_PREFIX + userId, Integer.toString(followId));
    }

    @Override
    public void closeConnection() {
        jedis.close();
        jedis.flushAll();
    }

    private void incrementLatestTweetId() {
        jedis.incr(LATEST_TWEET_ID_KEY);
    }

    private String getLatestTweetId() {
        return jedis.get(LATEST_TWEET_ID_KEY);
    }
}
