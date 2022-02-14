package Twitter;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Tweet {
    private final int tweetId;
    private final int userId;
    private final String tweetTxt;
    private final Timestamp tweetTS;

    public Tweet(int userId, String tweetTxt, Timestamp tweetTS) {
        if (tweetTxt == null || tweetTS == null) {
            throw new IllegalArgumentException("Cannot hand in a null value to create a tweet");
        }
        if (tweetTxt.length() > 140) {
            throw new IllegalArgumentException("tweet cannot be more than 140 chars");
        }
        this.tweetId = -1;
        this.userId = userId;
        this.tweetTxt = tweetTxt;
        this.tweetTS = tweetTS;
    }

    public Tweet(int tweetId, int userId, String tweetTxt, Timestamp tweetTS) {
        this.tweetId = tweetId;
        if (tweetTxt == null || tweetTS == null) {
            throw new IllegalArgumentException("Cannot hand in a null value to create a tweet");
        }
        if (tweetTxt.length() > 140) {
            throw new IllegalArgumentException("tweet cannot be more than 140 chars");
        }
        this.userId = userId;
        this.tweetTxt = tweetTxt;
        this.tweetTS = tweetTS;
    }
}
