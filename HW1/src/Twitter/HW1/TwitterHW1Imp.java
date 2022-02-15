package Twitter.HW1;

import Twitter.Tweet;
import Twitter.TwitterAPI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements the TwitterAPI Interface using a connection to a Relational Database (Specifically in the semantics of
 * PostgreSQL).
 */
public class TwitterHW1Imp implements TwitterAPI {
    private final RDBUtils RDBUtils;

    /**
     * Creates an instance of this class by establishing a connection to a relational database using the DBUtils Class.
     * @param user - the user accessing the database
     * @param password - the password of the user accessing the database
     * @param url - the URL of the database to connect to. Format can be one of {jdbc:postgresql:database,
     *           jdbc:postgresql://host/database, jdbc:postgresql://host:port/database}.
     */
    public TwitterHW1Imp(String user, String password, String url) {
        this.RDBUtils = new RDBUtils(url, user, password);
    }

    @Override
    public void postTweet(Tweet t) {
        this.RDBUtils.insertOneRecord(String.format("INSERT INTO tweet (user_id, tweet_ts, tweet_text) VALUES (%s, '%s', '%s')",
                t.getUserId(), t.getTweetTS(), t.getTweetTxt()));
    }

    @Override
    public List<Tweet> getTimeline(int userId) {
        String sql = String.format(
                "SELECT tweet.tweet_id as tweetId, follows.follows_id as userId, tweet.tweet_text as tweetTxt, " +
                        "tweet.tweet_ts as tweetTS FROM tweet JOIN follows on tweet.user_id = follows.follows_id " +
                        "WHERE follows.user_id = %s ORDER BY 4 DESC LIMIT 10",
                userId);

        return getTweetsWithQuery(sql, "tweetId", "userId", "tweetTxt", "tweetTS");
    }

    @Override
    public Set<Integer> getFollowers(int userId) {
        String sql = String.format(
                "SELECT follows_id FROM follows WHERE user_id = %s",
                userId);
        return new HashSet<>(getUserIdsWithQuery(sql, "follows_id"));
    }

    @Override
    public Set<Integer> getFollowees(int userId) {
        String sql = "SELECT user_id FROM follows WHERE follow_id = " + userId;
        return new HashSet<>(getUserIdsWithQuery(sql, "user_id"));
    }

    @Override
    public List<Tweet> getTweets(int userId) {
        String sql = "SELECT * FROM tweet WHERE tweet_id =" + userId;
        return getTweetsWithQuery(sql, "tweet_id", "user_id", "tweet_txt", "tweet_ts");
    }

    @Override
    public List<Integer> getAllUsers() {
        String sql = "SELECT user_id FROM tweet UNION SELECT user_id FROM follows UNION select follows_id FROM follows";
        return getUserIdsWithQuery(sql, "user_id");
    }

    @Override
    public void closeConnection() {
        RDBUtils.closeConnection();
    }

    /**
     * Helper function to help retrieve a list of Tweets from the relational DB
     * @param sql - the query to execute, must be a query that can extract a tweet object.
     * @param tweetIdCol - the alias for the tweet id
     * @param userIdCol - the alias for the user id
     * @param tweetTxtCol - the alias for the tweet text
     * @param tweetTSCol - the alias for the tweet timestamp
     * @return - a list of tweets from the result set of the executed query.
     */
    private List<Tweet> getTweetsWithQuery(String sql, String tweetIdCol, String userIdCol, String tweetTxtCol, String tweetTSCol) {
        List<Tweet> tweets = new ArrayList<>();

        try {
            Connection con = RDBUtils.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Tweet tweet = new Tweet(
                        rs.getInt(tweetIdCol),
                        rs.getInt(userIdCol),
                        rs.getString(tweetTxtCol),
                        rs.getTimestamp(tweetTSCol)
                );
                tweets.add(tweet);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return tweets;
    }

    /**
     * Helper function to help retrieve a list of user ids from the relational DB
     * @param sql - the query to execute, must be a query that can extract an id object.
     * @param user_id_col - the alias for the user id we are trying to extract
     * @return - a list of integers from the result set of the executed query.
     */
    private List<Integer> getUserIdsWithQuery(String sql, String user_id_col) {
        List<Integer> users = new ArrayList<>();
        try {
            Connection con = RDBUtils.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(rs.getInt(user_id_col));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        return users;
    }
}
