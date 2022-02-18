package ClientPrograms;

import Twitter.HW1.TwitterHW1Imp;
import Twitter.HW2.TwitterHW2Strat1Imp;
import Twitter.HW2.TwitterHW2Strat2Imp;
import Twitter.TwitterAPI;

/**
 * Factory class to create instances of twitter APIs to use in the client programs. Currently supports RDB, HW2Strat2,
 * and HWStrat1 implementations.
 */
public class TwitterFactory {
    /**
     * An enum of different twitter API types that can be used in client programs.
     */
    public static enum TwitterType {
        RDB, REDIS_STRAT1, REDIS_STRAT2
    }

    /**
     * A static factory method for creating instances of TwitterAPIs.
     * @param tt - the twitter type you would like to construct.
     * @return an instance of a twitter API to use in client programs.
     */
    public static TwitterAPI createTwitter(TwitterType tt, boolean flush) {
        switch (tt) {
            case RDB:
                return new TwitterHW1Imp(System.getenv("TWITTER_USER"), System.getenv("TWITTER_PASSWORD"), "jdbc:postgresql://localhost:5432/twitter");
            case REDIS_STRAT1:
                return new TwitterHW2Strat1Imp(flush);
            case REDIS_STRAT2:
                return new TwitterHW2Strat2Imp(flush);
            default:
                throw new IllegalArgumentException("Cannot constuct null twitter type");
        }

    }
}
