package com.PopCorp.Purchases.Receivers;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.Activities.MainActivity;
import com.PopCorp.Purchases.Fragments.ListFragment;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
	
	public static final String ALARM_INTENT_TITLE = "title";
	public static final String ALARM_INTENT_DATELIST = "datelist";
	
	@Override//создаем уведомление о сохраненном списке
	public void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WACELOCK Purchases");
		wakeLock.acquire();
		Intent intentForStartActivity = new Intent(context, MainActivity.class);
		if (intent!=null){
			intentForStartActivity.putExtra(ListFragment.INTENT_TO_LIST_TITLE, intent.getStringExtra(ALARM_INTENT_TITLE));
			intentForStartActivity.putExtra(ListFragment.INTENT_TO_LIST_DATELIST, intent.getStringExtra(ALARM_INTENT_DATELIST));

			createNotify(context, intentForStartActivity, intent.getStringExtra(ALARM_INTENT_TITLE), intent.getStringExtra(ALARM_INTENT_DATELIST));
			wakeLock.release();
		}
	}

	private void createNotify(Context context, Intent intentForStartActivity, String title, String datelist) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intentForStartActivity, 0);
		NotificationCompat.Builder notif = new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.ic_launcher)
		.setAutoCancel(true)
		.setContentText(context.getResources().getString(R.string.notify_time_to_shopping))
		.setContentTitle(title)
		.setDefaults(Notification.DEFAULT_ALL)
		.setTicker(context.getResources().getString(R.string.notify_time_to_shopping))
		.setWhen(System.currentTimeMillis())
		.setContentIntent(pi);
		
		Notification notification = notif.build();
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;
		notificationManager.notify(Integer.valueOf(datelist.substring(0, 6)), notification);
	}

	public void cancelAlarm(Context context, String name, String datelist) {
		Intent intent = new Intent(context, AlarmReceiver.class);//отмена уведомления
		intent.setAction(name + datelist);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	public void setAlarm(Context context, long dateInLong, String name, String datelist) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);//устанавливаем будильник
		intent.setAction(name + datelist);
		intent.putExtra(ALARM_INTENT_TITLE, name);
		intent.putExtra(ALARM_INTENT_DATELIST, datelist);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmManager.set(AlarmManager.RTC_WAKEUP, dateInLong, pendingIntent);
	}
}
