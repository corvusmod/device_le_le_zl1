package org.morphic.ts.extra;

import android.app.Service;
import android.os.IBinder;
import android.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.os.SystemProperties;

import android.provider.Settings;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import android.content.IntentFilter;

import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;
import android.os.UserHandle;

public class BootReceiver extends BroadcastReceiver 
{
	private static final String TAG = "TsBootReceiver";    

    @Override
    public void onReceive(Context context, Intent intent) 
    {
        String action = intent.getAction();
        Log.i(TAG, "onReceive " + action);

		// Check ROM update
		checkRomUpdate(context);

		//cp /system/etc/UEISettings /data/data/com.uei.quicksetsdk.letv/files/Settings
		//cpFile("/system/etc/UEISettings", "/data/data/com.uei.quicksetsdk.letv/files/Settings");

		try {
			SystemProperties.set("sys.init.uei", "1");		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private void cpFile(String src, String dst)
	{
		try {
			File dstFile = new File(dst);
			if (dstFile.exists())
				return;

			File srcFile = new File(src);
			copyWithStreams(srcFile, dstFile, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void copyWithStreams(File aSourceFile, File aTargetFile, boolean aAppend) 
	{
    	InputStream inStream = null;
    	OutputStream outStream = null;
    	try {
      		try {
        		byte[] bucket = new byte[1024];
        		inStream = new BufferedInputStream(new FileInputStream(aSourceFile));
        		outStream = new BufferedOutputStream(new FileOutputStream(aTargetFile, aAppend));
        		int bytesRead = 0;
        		while(bytesRead != -1) {
          			bytesRead = inStream.read(bucket); //-1, 0, or more
          			if (bytesRead > 0) {
            			outStream.write(bucket, 0, bytesRead);
					}
        		}
      		}
      		finally {
        		if (inStream != null) inStream.close();
        		if (outStream != null) outStream.close();
      		}
    	}
    	catch (Exception e){
      		e.printStackTrace();
    	}
  	}

	private void checkRomUpdate(Context context)
	{
		String val = SystemProperties.get("persist.sys.ts.bc");
        String val2 = SystemProperties.get("ro.build.date.utc");
        String title = "";
        String subject = "";
        String msg = "";        
        
        if (val.equals(""))
        {
            title = "Team Superluminal Slim Rom";
            msg = context.getResources().getString(R.string.newinstall_welcome);
            subject = context.getResources().getString(R.string.newinstall_subject);
            SystemProperties.set("persist.sys.ts.bc", val2);
        }
        else if (!val.equals(val2))
        {
            title = "Team Superluminal Slim Rom";
            msg = context.getResources().getString(R.string.update_welcome);
            subject = context.getResources().getString(R.string.update_subject);
            SystemProperties.set("persist.sys.ts.bc", val2);
        }
      
		if (!msg.equals(""))
		{
			int id = (int) System.currentTimeMillis();
		  
			Intent notifIntent = new Intent(context, SettingsActivity.class);
			notifIntent.setAction("OPEN");		
			PendingIntent pIntent = PendingIntent.getActivity(context, id, notifIntent, 0);

			Intent donateReceive = new Intent(context, SettingsActivity.class);  
			donateReceive.setAction("DONATE");		
			PendingIntent pendingIntentDonate = PendingIntent.getActivity(context, id, donateReceive, 0);
			
			// Build notification
			Notification noti = new Notification.Builder(context)
				.setAutoCancel(true)
				.setContentTitle(title)
				.setContentText(subject)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pIntent)
				.setStyle(new Notification.BigTextStyle().bigText(msg)) 
				.addAction(0, "Donate!", pendingIntentDonate)
				.addAction(0, "ROM Extra Settings", pIntent).build();
			
			// hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;

			NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(0, noti);
		}

	}
}
