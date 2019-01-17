package com.lnsf.example.botkuntweet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Status


class MainActivity : AppCompatActivity() {
    private var tweets: List<Tweet>? = null
    private var genSentence = ""

    private lateinit var twObj: TwitterObj

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val data = TwitterUtil.getSavedUserData(this)

        val token = data.first
        val tokenSecret = data.second
        val userId = data.third

        if (token == null || tokenSecret == null || userId == 0L) {
            val it = Intent(this, TwitterOAuthActivity::class.java)
            startActivity(it)
            finish()

            return
        }

        twObj = TwitterObj(
            TwitterUtil.getTwitterInstance(this)!!,
            userId
        )

        generate_button.setOnClickListener {
            it.isEnabled = false


            GlobalScope.launch {
                withContext(Dispatchers.Default) {
                    if (tweets == null) tweets = twObj.getUserTweets()
                }

                if (tweets == null) return@launch
                genSentence = TweetGenerator(tweets!!).generate()

                it.post {
                    generated_textview.text = genSentence
                    Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT)
                        .show()
                    it.isEnabled = true
                    tweet_button.isEnabled = true

                }
            }
        }

        tweet_button.setOnClickListener {
            it.isEnabled = false

            GlobalScope.launch {
                var status: Status? = null

                withContext(Dispatchers.Default) {
                    status = twObj.makeTweet(genSentence)
                }

                it.post {

                    Toast.makeText(
                        this@MainActivity,
                        if (status == null) "Tweet Failed" else "\"$genSentence\" Tweeted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}



