package com.bitlove.fetlife.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.bitlove.fetlife.BuildConfig;
import com.bitlove.fetlife.FetLifeApplication;
import com.bitlove.fetlife.R;
import com.bitlove.fetlife.model.pojos.fetlife.db.NotificationHistoryItem;
import com.bitlove.fetlife.view.screen.BaseActivity;
import com.bitlove.fetlife.view.screen.resource.ConversationsActivity;
import com.bitlove.fetlife.view.screen.resource.TurboLinksViewActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NewAnswerNotification extends OneSignalNotification {

    private static List<NewAnswerNotification> notifications = new ArrayList<>();

    public NewAnswerNotification(String title, String message, String launchUrl, JSONObject additionalData, String id, String group) {
        super(title, message, launchUrl, additionalData, id, group);
        notificationType = NotificationParser.JSON_VALUE_TYPE_QUESTIONS_NEW_ANSWER;
    }

    public static void clearNotifications() {
        synchronized (notifications) {
            notifications.clear();
        }
    }

    @Override
    public void display(FetLifeApplication fetLifeApplication) {
        synchronized (notifications) {
            notifications.add(this);

            String title = notifications.size() == 1 ? fetLifeApplication.getString(R.string.noification_summary_title_new_answer) : fetLifeApplication.getString(R.string.noification_summary_title_new_answers,notifications.size());

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(fetLifeApplication);
            NotificationCompat.Builder summaryNotificationBuilder = getDefaultNotificationBuilder(fetLifeApplication);

            List<Notification> newAnswersNotifications = getGroupedNotifications(fetLifeApplication, notifications);

            summaryNotificationBuilder
                    .setGroupSummary(true)
                    .setGroup(Integer.toString(OneSignalNotification.NOTIFICATION_ID_ANSWERS))
                    .setContentTitle(title)
                    .setContentText(title);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(fetLifeApplication.getString(R.string.noification_title_new_answers));
            inboxStyle.setSummaryText(title);

            for (Notification notification : newAnswersNotifications) {
                inboxStyle.addLine(notification.extras.getString(Notification.EXTRA_TITLE));
            }

            summaryNotificationBuilder.setStyle(inboxStyle);
            summaryNotificationBuilder.setContentIntent(null);

            notificationManager.notify(OneSignalNotification.NOTIFICATION_ID_ANSWERS, summaryNotificationBuilder.build());

            int i = OneSignalNotification.NOTIFICATION_ID_ANSWERS + 1;
            for (Notification notification : newAnswersNotifications) {
                notificationManager.notify(i++,notification);
            }

            onNotificationDisplayed(fetLifeApplication,NOTIFICATION_ID_DO_NOT_COLLAPSE);
        }
    }

    private List<Notification> getGroupedNotifications(FetLifeApplication fetLifeApplication, List<NewAnswerNotification> newAnswerNotifications) {
        LinkedHashMap<String,Integer> newAnswerGroups = new LinkedHashMap<>();
        LinkedHashMap<String,String> newAnswerUrls = new LinkedHashMap<>();
        for (NewAnswerNotification notification : newAnswerNotifications) {
            if (launchUrl == null) {
                continue;
            }
            List<String> launchUriSegments = Uri.parse(notification.launchUrl).getPathSegments();
            String groupId = launchUriSegments.size() > 1 ? launchUriSegments.get(1) : "";
            Integer newAnswerCount = newAnswerGroups.get(groupId);
            if (newAnswerCount == null) {
                newAnswerCount = 1;
            } else {
                newAnswerCount++;
            }
            newAnswerGroups.put(groupId,newAnswerCount);
            newAnswerUrls.put(groupId,notification.launchUrl);
        }
        List<Notification> notifications = new ArrayList<>();
        int i = OneSignalNotification.NOTIFICATION_ID_ANSWERS + 1;
        for (Map.Entry<String,Integer> newAnswerGroup : newAnswerGroups.entrySet()) {
            NotificationCompat.Builder notificationBuilder = getDefaultNotificationBuilder(fetLifeApplication);
            notificationBuilder.setContentIntent(getPendingIntent(fetLifeApplication,newAnswerUrls.get(newAnswerGroup.getKey()),i++));
            notificationBuilder.setGroup(Integer.toString(OneSignalNotification.NOTIFICATION_ID_ANSWERS));
            notificationBuilder.setContentTitle(fetLifeApplication.getString(R.string.noification_title_new_answers));
            notificationBuilder.setContentText(new Integer(1).equals(newAnswerGroup.getValue()) ? fetLifeApplication.getString(R.string.noification_text_new_answer) : fetLifeApplication.getString(R.string.noification_text_new_answers,newAnswerGroup.getValue()));
            notifications.add(notificationBuilder.build());
        }
        Collections.reverse(notifications);
        return notifications;
    }


    @Override
    public void onNotificationDisplayed(FetLifeApplication fetLifeApplication, int notificationId) {
        NotificationHistoryItem notificationHistoryItem = createNotificationItem(notificationId, null);
        notificationHistoryItem.save();
    }

    @Override
    PendingIntent getPendingIntent(Context context) {
        return null;
    }

    private PendingIntent getPendingIntent(Context context, String launchUrl, int requestCode) {
        Intent baseIntent = TurboLinksViewActivity.createIntent(context,"q",context.getString(R.string.title_activity_questions), true, TurboLinksViewActivity.FAB_LINK_NEW_QUESTION,false);
        Intent contentIntent = TurboLinksViewActivity.createIntent(context,launchUrl.replaceAll("//fetlife.com","//app.fetlife.com"),null, false, null,false);
        contentIntent.putExtra(BaseActivity.EXTRA_NOTIFICATION_SOURCE_TYPE,getNotificationType());
//        return PendingIntent.getActivity(context, requestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return TaskStackBuilder.create(context).addNextIntentWithParentStack(baseIntent).addNextIntent(contentIntent).getPendingIntent(requestCode,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean handle(FetLifeApplication fetLifeApplication) {
        return false;
    }

    @Override
    public void onClick(FetLifeApplication fetLifeApplication) {

    }

    @Override
    public String getAssociatedPreferenceKey(Context context) {
        return context.getString(R.string.settings_key_notification_new_answers_enabled);
    }
}
