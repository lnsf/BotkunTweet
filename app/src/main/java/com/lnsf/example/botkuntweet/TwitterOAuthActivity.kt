package com.lnsf.example.botkuntweet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import twitter4j.Twitter

class TwitterOAuthActivity : AppCompatActivity() {

    private var callbackURL = ""
    private lateinit var authUtil: TwitterUtil.AuthUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callbackURL = getString(R.string.callback_url)

        val tw = TwitterUtil.getTwitterInstance(this)

        if (tw != null)
            startAuth(tw)


    }

    override fun onNewIntent(intent: Intent?) {
        if (intent == null || intent.data == null) return

        val verifier = intent.data.getQueryParameter("oauth_verifier") ?: return

        GlobalScope.launch {
            val access = withContext(Dispatchers.Default) {
                authUtil.grantAccessToken(verifier)
            } ?: return@launch

            TwitterUtil.writeInfoToSharedPref(this@TwitterOAuthActivity, access)

            val it = Intent(this@TwitterOAuthActivity, MainActivity::class.java)

            startActivity(it)

            finish()
        }
    }

    private fun startAuth(tw: Twitter) {
        authUtil = TwitterUtil.AuthUtil(tw)

        GlobalScope.launch {
            val url = withContext(Dispatchers.Default) {
                authUtil.getAuthURL()
            } ?: return@launch

            val it = CustomTabsIntent
                .Builder()
                .build()

            it.launchUrl(this@TwitterOAuthActivity, Uri.parse(url))
        }
    }
}
