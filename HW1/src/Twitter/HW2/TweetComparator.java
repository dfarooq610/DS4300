package Twitter.HW2;

import Twitter.Tweet;
import java.util.Comparator;

/**
 * Function Object representing a comparator between two tweet objects based on timestamp.
 */
public class TweetComparator implements Comparator<Tweet> {

    @Override
    // compares the timestamps of the two tweets
    public int compare(Tweet t1, Tweet t2) {
        if (t1.equals(t2)) {
            return 0;
        }
        return t1.getTweetTS().compareTo(t2.getTweetTS());
    }
}
