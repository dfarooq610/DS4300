package Twitter.HW2;

import Twitter.Tweet;
import Twitter.TwitterAPI;
import redis.clients.jedis.Jedis;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements the TwitterAPI Interface using a connection to Redis, implementing Strategy 2 from the HW Doc
 * <p>
 * All userIds are put into a global set of users.
 * <p>
 * For each user, we keep track of their timeline, tweets, followers, and follows. The timeline and tweets are
 * Lists of tweetIds, to keep the lists lightweight, and are ordered by posting time. The followers and follows are
 * sets, since order doesn’t matter, allowing for quick lookup.
 * <p>
 * Tweets are stored as individual hashed objects. For each hashed tweet, we store a timestamp and the tweet text.
 * TweetIDs are incremented using a latestTweetId.
 */
public class TwitterHW2Strat2Imp implements TwitterAPI {
    protected final Jedis jedis;

    public static String LATEST_TWEET_ID_KEY = "latestTweetId";
    public static String FOLLOWERS_PREFIX = "followers:";
    public static String FOLLOWS_PREFIX = "follows:";
    public static String TIMELINE_PREFIX = "timeline:";
    public static String TWEETS_PREFIX = "tweets:";
    public static String USERS_SET = "users";
    public static String TWEET_HASH_KEY = "tweet:";
    public static String TWEET_TXT_KEY = "tweetTxt";
    public static String TWEET_TS_KEY = "tweetTS";

    /**
     * Constructs an instance of a TwitterHW2Strat2Imp with Jedis as the main driver.
     */
    public TwitterHW2Strat2Imp() {
        this.jedis = new Jedis();
        jedis.set("latestTweetId", "1");
    }

    /**
     * Constructs an instance of the TwitterHW2Strat2Imp, with the given option to flush what is currently in the db.
     *
     * @param flush - a boolean representing if we should flush the db or not. Flush if true, else do nothing.
     */
    public TwitterHW2Strat2Imp(boolean flush) {
        this();
        if (flush) {
            jedis.flushAll();
        }
    }


    @Override
    public void postTweet(Tweet t) {
        String latestTweetId = getLatestTweetId();
        // add tweet to database with the latest tweet id in the form userId tweetTxt tweetTS
        this.jedis.hset(TWEET_HASH_KEY + latestTweetId, TWEET_TS_KEY, t.getTweetTS().toString());
        this.jedis.hset(TWEET_HASH_KEY + latestTweetId, TWEET_TXT_KEY, t.getTweetTxt());
        Set<Integer> followers = this.getFollowers(t.getUserId()); // get followers of the user who posted this tweet
        // for each follower, add tweet id to their timeline
        for (Integer follower : followers) {
            this.jedis.lpush(TIMELINE_PREFIX + follower, latestTweetId);
        }
        this.jedis.lpush(TWEETS_PREFIX + t.getUserId(), latestTweetId);
        incrementLatestTweetId();
    }

    @Override
    public List<Tweet> getTimeline(int userId) {
        return getMostRecentTweetsFrom(userId, TIMELINE_PREFIX, 0, 10);
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
        return getMostRecentTweetsFrom(userId, TWEETS_PREFIX, 0, -1);
    }

    @Override
    public List<Integer> getAllUsers() {
        return jedis.smembers(USERS_SET).stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    @Override
    public void addFollow(int userId, int followId) {
        jedis.sadd(USERS_SET, Integer.toString(userId));
        jedis.sadd(USERS_SET, Integer.toString(followId));
        // add the userId to the followId's followers
        jedis.sadd(FOLLOWERS_PREFIX + followId, Integer.toString(userId));
        // add the followID to the userId's follows
        jedis.sadd(FOLLOWS_PREFIX + userId, Integer.toString(followId));
    }

    @Override
    public void closeConnection() {
        jedis.close();
    }

    protected void incrementLatestTweetId() {
        jedis.incr(LATEST_TWEET_ID_KEY);
    }

    protected String getLatestTweetId() {
        return jedis.get(LATEST_TWEET_ID_KEY);
    }

    protected List<Tweet> getMostRecentTweetsFrom(int userId, String tweetSource, int start, int end) {
        // get latest 10 tweetIDs from jedis, and convert them to ints
        List<Integer> tweetIds = jedis.lrange(tweetSource + userId, start, end).stream()
                .map(Integer::parseInt).collect(Collectors.toList());

        // for each tweet, get the tweet hash info, parse it, and convert it to a Tweet object
        return tweetIds.stream().map(id -> {
            String tweetTxt = jedis.hget(TWEET_HASH_KEY + id, TWEET_TXT_KEY);
            Timestamp tweetTs = Timestamp.valueOf(jedis.hget(TWEET_HASH_KEY + id, TWEET_TS_KEY));
            return new Tweet(id, userId, tweetTxt, tweetTs);
        }).collect(Collectors.toList());
    }
}
