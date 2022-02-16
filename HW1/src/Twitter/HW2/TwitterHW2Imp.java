package Twitter.HW2;

import Twitter.Tweet;
import Twitter.TwitterAPI;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Set;

/**
 * Implements the TwitterAPI Interface using a connection to Redis, implementing Strategy 2 from the HW Doc
 * (TODO: elaborate more about schema)
 */
public class TwitterHW2Imp implements TwitterAPI {
    private Jedis jedis;

    public TwitterHW2Imp() {
        this.jedis = new Jedis();
        jedis.flushAll();
    }

    @Override
    public void postTweet(Tweet t) {

    }

    @Override
    public List<Tweet> getTimeline(int userId) {
        return null;
    }

    @Override
    public Set<Integer> getFollowers(int userId) {
        return null;
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
        return null;
    }

    @Override
    public void addFollow(int userId, int followId) {
        // if the user does not already exist, create a followers and follows list for them.
        // if the follow does not already exist, create a followers and follows list for them.
        // add the userId to the followId's followers
        // add the followID to the userId's follows (**NECESSARY FOR GET TIMELINE**)
    }

    @Override
    public void closeConnection() {

    }
}
