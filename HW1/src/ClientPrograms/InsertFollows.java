package ClientPrograms;

import Twitter.HW2.TwitterHW2Imp;
import Twitter.TwitterAPI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InsertFollows {
    private static final String COMMA_DELIMITER = ",";
    public static String FILENAME = "data/test/follows.csv";

    public static void main(String[] args) {
        long begin = System.nanoTime();
        int i = addAllFollowers();
        long end = System.nanoTime();
        System.out.println("total follows: " + i);
        System.out.println("time elapsed: " + intervalToSeconds(begin, end));
        System.out.println("follows per sec: " + (i / intervalToSeconds(begin, end)));
    }

    /**
     * Handy helper method that tells us the duration between the start and end in seconds
     *
     * @param start - the start time in nanoseconds
     * @param end   - the end time in nanoseconds
     * @return - the duration between the start and end in seconds
     */
    public static double intervalToSeconds(long start, long end) {
        return (end - start) / 1e9;
    }

    private static int addAllFollowers() {
        TwitterAPI api = new TwitterHW2Imp();
        int i = 0;
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(FILENAME));
            br.readLine();
            while ((line = br.readLine()) != null) {
                List<Integer> ids = getFollowFromLine(line);
                api.addFollow(ids.get(0), ids.get(1));
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

    private static List<Integer> getFollowFromLine(String line) {
        List<String> values = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return List.of(
          Integer.parseInt(values.get(0)),
          Integer.parseInt(values.get(1))
        );
    }
}
