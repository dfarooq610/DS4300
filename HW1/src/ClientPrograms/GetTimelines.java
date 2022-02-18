package ClientPrograms;

import Twitter.TwitterAPI;

import java.util.List;
import java.util.Random;

/**
 * A small script to repeatedly get the timelines of users in the twitter application. First it establishes a connection
 * to the db, then receives all the users in the application. Then, for the duration of 100 seconds, we choose a
 * random user in the db and get their timeline.
 * <p>
 * To test this under the fullest load, ensure there are ample tweets and following information in the DB.
 * This is by running the PostTweets Program first, and importing the follows CSV.
 * <p>
 * For a postgres db, you can do this by running
 * "psql -c "\copy  follows from  '<FULL_CSV_LOCATION>' DELIMITER ',' CSV HEADER" -U <TWITTER_USER> -d <DB_NAME>
 * <p>
 * For Redis, run the InsertFollows Program.
 */
public class GetTimelines {

    public static void main(String[] args) {
        TwitterAPI api = TwitterFactory.createTwitter(TwitterFactory.TwitterType.REDIS_STRAT2, false);
        List<Integer> userIds = api.getAllUsers();
        System.out.println("Choosing from " + userIds.size() + " users total");

        long end = 0;
        int timelines = 0;
        long begin = System.nanoTime();
        // for 100 seconds, choose a random user and fetch their timeline
        while (PostTweets.intervalToSeconds(begin, end) < 500) {
            Random rand = new Random();
            int randomUser = userIds.get(rand.nextInt(userIds.size()));
            api.getTimeline(randomUser);
            timelines += 1;
            end = System.nanoTime();
        }
        api.closeConnection();
        System.out.println("time elapsed: " + PostTweets.intervalToSeconds(begin, end));
        System.out.println("total timelines: " + timelines);
        System.out.println("timelines per sec: " + timelines / PostTweets.intervalToSeconds(begin, end));
    }
}
