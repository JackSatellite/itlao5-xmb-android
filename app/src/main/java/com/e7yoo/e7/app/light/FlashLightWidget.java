package com.e7yoo.e7.app.light;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.e7yoo.e7.R;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Implementation of App Widget functionality.
 */
public class FlashLightWidget extends AppWidgetProvider {

    public static final String ACTION_LED_ON = "action_led_on";
    public static final String ACTION_LED_OFF = "action_led_off";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_LED_ON.equals(action)) {
            CameraManager.openFlash(context);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, FlashLightWidget.class);
            int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.flash_light_widget);
            views.setImageViewResource(R.id.widget_led, R.mipmap.bg_led_on_widget);
            views.setInt(R.id.widget_led, "setBackgroundResource", R.mipmap.bg_led_on_widget_bg);
            views.setOnClickPendingIntent(R.id.widget_led, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_LED_OFF), 0));
            appWidgetManager.updateAppWidget(widgetIds, views);

            NotificationControl.showNotification(context);
        } else if (ACTION_LED_OFF.equals(action)) {
            CameraManager.closeFlash();
            try {
                CameraManager.release();
            } catch (Throwable e) {
                //45 java.lang.RuntimeException    Camera is being used after Camera.release() was called
                CrashReport.postCatchedException(e);
            }
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, FlashLightWidget.class);
            int[] widgetIds = appWidgetManager.getAppWidgetIds(componentName);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.flash_light_widget);
            views.setImageViewResource(R.id.widget_led, R.drawable.widget_led);
            views.setInt(R.id.widget_led, "setBackgroundResource", R.drawable.widget_led_bg);
            views.setOnClickPendingIntent(R.id.widget_led, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_LED_ON), 0));
            appWidgetManager.updateAppWidget(widgetIds, views);

            NotificationControl.cancelNotification(context);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int widgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.flash_light_widget);
        views.setImageViewResource(R.id.widget_led, R.drawable.widget_led);
        views.setInt(R.id.widget_led, "setBackgroundResource", R.drawable.widget_led_bg);
        views.setOnClickPendingIntent(R.id.widget_led, PendingIntent.getBroadcast(context, 0, new Intent(ACTION_LED_ON), 0));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


