package com.example.bgps.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TripActivityReceiver : BroadcastReceiver() {

    @Inject lateinit var sampler: TripActivitySampler

    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityRecognitionResult.hasResult(intent)) return
        val result = ActivityRecognitionResult.extractResult(intent) ?: return
        sampler.add(result.mostProbableActivity.type)
    }

    companion object {
        const val ACTION_ACTIVITY_UPDATE = "com.example.bgps.action.ACTIVITY_UPDATE"
    }
}
