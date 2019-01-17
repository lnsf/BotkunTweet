package com.lnsf.example.botkuntweet

import twitter4j.Paging
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterException

class TwitterObj(
    var twitter: Twitter,
    var userId: Long
) {
    fun getUserTweets(): List<Tweet>? {
        try {
            val userTweets = twitter.getUserTimeline(userId, Paging(1, 200))

            val t = mutableListOf<Tweet>()

            userTweets.forEach tweet@{ tweet ->
                var txt = ""
                if (
                    tweet.text.startsWith("@") ||       // リプの除外
                    tweet.text.startsWith("RT @") ||    // RTの除外
                    tweet.text.contains("http")         //　URLつきツイートの除外
                ) return@tweet

                tweet.text.split(' ').forEach {
                    if (it.contains('#') ||             // ハッシュタグは除く
                        it.contains('@')                // ユーザー名は除く
                    ) return@tweet

                    txt += " $it"   // #.split(' ') で消えたスペースを補う
                }

                txt = txt.removeSurrounding(" ")    // 周辺に付いた空白を除去

                t.add(Tweet(txt, tweet.id))
            }

            return t

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun makeTweet(s: String) : Status?{
        return try {
            twitter.updateStatus("$s\n#botkuntweet")
        }catch (e: TwitterException){
            null
        }
    }
}