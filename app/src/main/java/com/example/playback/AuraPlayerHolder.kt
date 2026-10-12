package com.example.playback

import android.content.Context
import androidx.media3.common.util.UnstableApi

@UnstableApi
object AuraPlayerHolder {
    @Volatile
    private var instance: AuraAudioPlayer? = null

    fun getPlayer(context: Context): AuraAudioPlayer {
        return instance ?: synchronized(this) {
            instance ?: AuraAudioPlayer(context.applicationContext).also {
                instance = it
            }
        }
    }
}
