package Twitter;

import java.util.List;
import java.util.Set;

/**
 * Represents a collection of core operations for Twitter. Includes the ability to view information about a user's
 * social network, allow users to interact with the app, among other things. Also allows the ability to deactivate the
 * connection to the DB through the API.
 */
public interface TwitterAPI {

    /**
     * Adds a new tweet to Twitter.
     * @param t - A tweet object
     */
    void postTweet(Tweet t);

    /**
     * Returns the latest 10 tweets from the people that the specified user follows.
     * @param userId  - the id of the user to fetch the timeline for
     * @return - a list of 10 tweets to represent a "timeline"
     */
    List<Tweet> getTimeline(int userId);

    /**
     * Receive all the user ids that follow the given user.
     * @param userId - the id of the user to get the followers of
     * @return - a list of integers representing the user ids that follow the given user.
     */
    Set<Integer> getFollowers(int userId);

    /**
     * Receive all the user ids that the given user follows.
     * @param userId - the id of the user to get their follows.
     * @return - a list of integers of user ids that this user follows.
     */
    Set<Integer> getFollowees(int userId);

    /**
     * Receive all the tweets for a specified user.
     * @param userId - the id of the user to fetch tweets for.
     * @return - a list of Tweet objects created by the user.
     */
    List<Tweet> getTweets(int userId);

    /**
     * Returns the universe of all twitter accounts -- all possible user ids.
     * @return - a list of user id's for all of the accounts in the application
     */
    List<Integer> getAllUsers();

    /**
     * The given userId will follow the user with the followId.
     */
    void addFollow(int userId, int followId);

    /**
     * Closes the connection to the database.
     */
    void closeConnection();
}
