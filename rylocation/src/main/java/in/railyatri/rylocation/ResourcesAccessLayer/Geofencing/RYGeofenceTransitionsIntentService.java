/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.railyatri.rylocation.ResourcesAccessLayer.Geofencing;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import icom.realtimelocation.realtimeloc.R;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import in.railyatri.rylocation.BusinessLayer.BusinessEvent.EventGeofenceFound;


/**
 * Listener for geofence transition changes.
 */
public class RYGeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "RYGeofenceTransitionsIS";

    public RYGeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Handles incoming intents.
     */

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = RYGeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceType = geofencingEvent.getGeofenceTransition();

        if (geofenceType == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceType == Geofence.GEOFENCE_TRANSITION_EXIT || geofenceType == Geofence.GEOFENCE_TRANSITION_DWELL) {

            List<Geofence> geofencesList = geofencingEvent.getTriggeringGeofences();

//            showNotification(geofenceType, geofencesList);

            EventGeofenceFound eventGeofence = new EventGeofenceFound();
            eventGeofence.setEventType(geofenceType);
            eventGeofence.setGeofenceList(geofencesList);
            EventBus.getDefault().post(eventGeofence);


            Log.i(TAG, geofenceType + " , " + geofencesList.get(0).getRequestId());
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceType));
        }
    }


    /**
     * Show Notification for Testing
     */

//    private static final int MY_NOTIFICATION_ID = 1;
//    NotificationManager notificationManager;
//    Notification myNotification;
//
//    private void showNotification(int geofenceType, List<Geofence> geofenceList) {
//
//        final String notificationDetails = getGeofenceTransitionDetails(
//                this,
//                geofenceType,
//                geofenceList
//        );
//
//
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                myNotification = new NotificationCompat.Builder(context,PRIMARY_NOTIF_CHANNEL)
//                        .setContentTitle("Notify")
//                        .setContentText(notificationDetails)
//                        .setTicker("Notification!")
//                        .setWhen(System.currentTimeMillis())
//                        .setDefaults(Notification.DEFAULT_SOUND)
//                        .setAutoCancel(true)
//                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
//                        .build();
//
//                notificationManager.notify(MY_NOTIFICATION_ID, myNotification);
//
//            }
//        });
//    }


    /**
     * @param transitionType
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<String>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }
}
