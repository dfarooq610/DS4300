package Twitter.HW2;

import Twitter.Tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implements Strategy1 of HW2 by extending the Strategy2 class. The schema remains the same, but the postTweet and
 * getTimeline methods are different. We no longer post the tweet to all the follower's timelines. In getTimeline, we
 * now aggregate all the user's follow's tweets, sort them, and return the newest 10.
 */
public class TwitterHW2Strat1Imp extends TwitterHW2Strat2Imp {

    /**
     * Creates an instance of a HW2Strat1Impl using the super constructor to initialize Jedis.
     */
    public TwitterHW2Strat1Imp() {
        super();
    }

    /**
     * Creates an instance of a HW2Strat1Impl using the super constructor to initialize Jedis.
     *
     * @param flush - whether we flush the database upon construction.
     */
    public TwitterHW2Strat1Imp(boolean flush) {
        super(flush);
    }

    @Override
    public void postTweet(Tweet t) {
        String latestTweetId = getLatestTweetId();
        // add tweet to database with the latest tweet id in the form userId tweetTxt tweetTS
        this.jedis.hset(TWEET_HASH_KEY + latestTweetId, TWEET_TS_KEY, t.getTweetTS().toString());
        this.jedis.hset(TWEET_HASH_KEY + latestTweetId, TWEET_TXT_KEY, t.getTweetTxt());
        Set<Integer> followers = this.getFollowers(t.getUserId()); // get followers of the user who posted this tweet

        this.jedis.lpush(TWEETS_PREFIX + t.getUserId(), latestTweetId);
        incrementLatestTweetId();
    }

    @Override
    public List<Tweet> getTimeline(int userId) {
        List<Integer> follows = new ArrayList<>(this.getFollowees(userId));
        List<Tweet> allTweets = new ArrayList<Tweet>();
        for (int followId : follows) {
            allTweets.addAll(this.getMostRecentTweetsFrom(followId, TWEETS_PREFIX, 0, 10));
        }
        allTweets.sort(new TweetComparator());
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (allTweets.size() > 0) {
                tweets.add(allTweets.remove(allTweets.size() - 1));
            }
        }
        return tweets;
    }

}
