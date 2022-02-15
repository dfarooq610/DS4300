DROP DATABASE IF EXISTS twitter;
CREATE DATABASE twitter;

DROP TABLE IF EXISTS tweet;

CREATE TABLE tweet
(
    tweet_id serial PRIMARY KEY,
    user_id BIGINT,
    tweet_ts TIMESTAMP,
    tweet_text VARCHAR ( 140 )
);

-- Who follows whom. The user “user_id” follows the user “follows_id”
DROP TABLE IF EXISTS follows;
CREATE TABLE follows
(
    user_id BIGINT,
    follows_id BIGINT
);

CREATE INDEX idx_user_tweet ON tweet (user_id);
CREATE INDEX idx_user_follower ON follows (user_id, follows_id);