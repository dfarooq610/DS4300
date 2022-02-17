package ClientPrograms;

import Twitter.HW2.TwitterHW2Strat2Imp;
import Twitter.Tweet;
import Twitter.TwitterAPI;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Program to performance test how many tweets we can post. Reads in a CSV of tweets delimited by a comma, and
 * constructs a tweet object for each of them. Uploads 1 tweet to the API at a time. For best results, we can
 * ensure that there are no tweets in the db to begin with (so we start out on a clean slate) and for neatness,
 * it's nice if the IDs are sequenced at 1.
 *
 * The way to reset the tweets DB in a relational db is to run: "DELETE FROM follows;" then
 * "ALTER SEQUENCE tweet_tweet_id_seq RESTART WITH 1;"
 *
 * To reset everything in a key-value store, you can run FLUSHALL.
 *
 * Either way, ensure that the follows table has been inserted prior to running.
 */
public class PostTweets {
    private static final String COMMA_DELIMITER = ",";
    public static String FILENAME = "data/test/tweet.csv";

    public static void main(String[] args) {
        long begin = System.nanoTime();
        int i = postAllTweets();
        long end = System.nanoTime();
        System.out.println("total tweets: " + i);
        System.out.println("time elapsed: " + intervalToSeconds(begin, end));
        System.out.println("tweets per sec: " + (i / intervalToSeconds(begin, end)));
    }

    /**
     * Handy helper method that tells us the duration between the start and end in seconds
     * @param start - the start time in nanoseconds
     * @param end - the end time in nanoseconds
     * @return - the duration between the start and end in seconds
     */
    public static double intervalToSeconds(long start, long end) {
        return (end - start) / 1e9;
    }

    private static int postAllTweets() {
        TwitterAPI api = TwitterFactory.createTwitter(TwitterFactory.TwitterType.REDIS_STRAT2);
        int i = 0;
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(FILENAME));
            br.readLine();
                while ((line = br.readLine()) != null) {
                    api.postTweet(getTweetFromLine(line));
                    i += 1;
                }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + FILENAME);
        } catch (IOException e) {
            System.out.println("IO EXCEPTION: SOMETHING BAD HAPPENED");
        }
        api.closeConnection();
        return i;
    }

    private static Tweet getTweetFromLine(String line) {
        List<String> values = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return new Tweet(
                Integer.parseInt(values.get(0)),
                values.get(1),
                new Timestamp(System.currentTimeMillis())
        );
    }
}
