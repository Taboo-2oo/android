package com.bitlove.fetlife.inbound.onesignal.notification

import android.app.PendingIntent
import android.content.Context
import com.bitlove.fetlife.R
import com.bitlove.fetlife.view.screen.BaseActivity
import com.bitlove.fetlife.webapp.screen.FetLifeWebViewActivity
import org.json.JSONObject

class RequestNotification(notificationType: String, notificationIdRange: Int, title: String, message: String, launchUrl: String, mergeId: String?, collapseId: String?, additionalData: JSONObject, preferenceKey: String?) : OneSignalNotification(notificationType, notificationIdRange, title, message, launchUrl, mergeId, collapseId, additionalData, preferenceKey) {

    //TODO: open real content, and/or handle if notification screen is open

    override fun getNotificationChannelName(context: Context): String? {
        return context.getString(R.string.settings_title_notification_friendrequests_enabled)
    }

    override fun getNotificationChannelDescription(context: Context): String? {
        return context.getString(R.string.settings_summary_notification_friendrequests_enabled)
    }

    override fun getSummaryTitle(notificationCount: Int, context: Context): String? {
        return context.resources.getQuantityString(R.plurals.noification_summary_title_requests_new_friendrequest, notificationCount, notificationCount)
    }

    override fun getSummaryText(notificationCount: Int, context: Context): String? {
        return context.getString(R.string.noification_summary_text_requests_new_friendrequest)
    }

    override fun getNotificationTitle(oneSignalNotification: OneSignalNotification, notificationCount: Int, context: Context): String? {
        return if (notificationCount == 1) {
            super.getNotificationTitle(oneSignalNotification, notificationCount, context)
        } else {
            return context.getString(R.string.noification_title_comments_requests_new_friendrequest, notificationCount)
        }
    }

    override fun getNotificationText(oneSignalNotification: OneSignalNotification, count: Int, context: Context): String? {
        return oneSignalNotification.message?.toLowerCase()
    }

    override fun getNotificationIntent(oneSignalNotification: OneSignalNotification, context: Context, order: Int): PendingIntent? {
        val contentIntent = FetLifeWebViewActivity.createIntent(context, "requests", true, R.id.navigation_bottom_requests, true, null).apply {
            putExtra(BaseActivity.EXTRA_NOTIFICATION_SOURCE_TYPE, oneSignalNotification.notificationType)
            putExtra(BaseActivity.EXTRA_NOTIFICATION_MERGE_ID, oneSignalNotification.mergeId)
        }
        return PendingIntent.getActivity(context,order,contentIntent,PendingIntent.FLAG_CANCEL_CURRENT)
    }

}