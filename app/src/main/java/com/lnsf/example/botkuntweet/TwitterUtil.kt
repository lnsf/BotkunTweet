package com.lnsf.example.botkuntweet

import android.content.Context
import twitter4j.Paging
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import java.lang.Exception

object TwitterUtil {
    private const val sharedPrefName = "KEYS"
    private const val accessTokenPrefName = "ACCESS_TOKEN"
    private const val accessTokenSecretPrefName = "ACCESS_TOKEN_SECRET"
    private const val userIdPrefName = "USER_ID"

    fun getSavedUserData(c: Context) : Triple<String?, String?, Long> {
        val keys = c.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val at = keys.getString(accessTokenPrefName, null)
        val ats = keys.getString(accessTokenSecretPrefName, null)
        val id = keys.getLong(userIdPrefName, 0L)

        return Triple(at, ats, id)
    }

    fun getTwitterInstance(context: Context): Twitter? {
        val tw = TwitterFactory().instance

        val cKey = context.getString(R.string.consumer_key)
        val cKeySec = context.getString(R.string.consumer_key_secret)

        tw.setOAuthConsumer(cKey, cKeySec)

        val info = getSavedUserData(context)

        if(info.first != null && info.second != null)
            tw.oAuthAccessToken = AccessToken(info.first, info.second)

        return tw
    }

    fun writeInfoToSharedPref(c: Context, access : AccessToken){
        val pref = c.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString(accessTokenPrefName, access.token)
        editor.putString(accessTokenSecretPrefName, access.tokenSecret)
        editor.putLong(userIdPrefName, access.userId)

        editor.apply()
    }

    class AuthUtil(private val twitter: Twitter){
        private var requestToken: RequestToken? = null

        fun getAuthURL(): String?{
            return try{
                requestToken = twitter.oAuthRequestToken
                requestToken?.authorizationURL
            } catch (te: TwitterException){
                te.printStackTrace()
                null
            }
        }

        fun grantAccessToken(verifier: String): AccessToken?{
            return try {
                twitter.getOAuthAccessToken(requestToken, verifier)
            }catch (te: TwitterException){
                te.printStackTrace()
                null
            }
        }
    }
}

