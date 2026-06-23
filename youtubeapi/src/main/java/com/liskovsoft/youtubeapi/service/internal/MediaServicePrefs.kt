package com.liskovsoft.youtubeapi.service.internal

import android.annotation.SuppressLint
import com.liskovsoft.mediaserviceinterfaces.SignInService.OnAccountChange
import com.liskovsoft.mediaserviceinterfaces.oauth.Account
import com.liskovsoft.sharedutils.misc.WeakHashSet
import com.liskovsoft.sharedutils.prefs.SharedPreferencesBase
import com.liskovsoft.youtubeapi.app.AppService
import com.liskovsoft.youtubeapi.service.YouTubeSignInService

private const val PREF_NAME = "yt_service_prefs"

@SuppressLint("StaticFieldLeak")
internal object MediaServicePrefs: SharedPreferencesBase(AppService.instance().context, PREF_NAME), OnAccountChange {
    private const val ANONYMOUS_PROFILE_NAME = "anonymous"
    private val mListeners = WeakHashSet<ProfileChangeListener>()
    private lateinit var mProfileName: String

    interface ProfileChangeListener {
        fun onProfileChanged()
    }

    init {
        val signInService = YouTubeSignInService.instance()
        setProfileName(signInService.selectedAccount)
        signInService.addOnAccountChange(this)
    }

    override fun onAccountChanged(account: Account?) {
        setProfileName(account)
        notifyListeners()
    }

    private fun notifyListeners() {
        mListeners.forEach { it.onProfileChanged() }
    }

    private fun setProfileName(account: Account?) {
        mProfileName = account?.name?.replace(" ", "_") ?: ANONYMOUS_PROFILE_NAME
    }

    fun addListener(listener: ProfileChangeListener) {
        mListeners.add(listener)
    }

    // RayNeo fork: use old SharedModules prefs API (getString/putString) so we can keep
    // MediaServiceCore bumped (search fix) without bumping SharedModules (which would break the 31.45 app).
    private fun readData(key: String): String? {
        return getString(key, null)
    }

    private fun writeData(key: String, data: String?) {
        putString(key, data)
    }

    fun getProfileData(key: String): String? {
        return readData(getProfileDataKey(key))
    }

    fun setProfileData(key: String, data: String?) {
        writeData(getProfileDataKey(key), data)
    }

    private fun getProfileDataKey(dataKey: String) = "${mProfileName}_$dataKey"
}