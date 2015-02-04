package com.mopub.mobileads;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import java.util.WeakHashMap;

final class FlurryAgentWrapper {
    private static FlurryAgentWrapper sWrapper;

    public static synchronized FlurryAgentWrapper getInstance() {
        if (sWrapper == null) {
            sWrapper = new FlurryAgentWrapper();
        }

        return sWrapper;
    }

    private final WeakHashMap<Context, Boolean> mContextMap = new WeakHashMap<Context, Boolean>();

    private FlurryAgentWrapper() {
        FlurryAgent.setLogEnabled(false);
        FlurryAgent.setLogLevel(Log.INFO);
        FlurryAgent.addOrigin("Flurry_Mopub_Android", "5.0.0.r1");
    }

    public synchronized void onStartSession(Context context, String apiKey) {
        // validate parameters
        if (context == null || TextUtils.isEmpty(apiKey)) {
            return;
        }

        // init
        FlurryAgent.init(context, apiKey);

        // sessions are automatic on ICS+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        }

        // only allow one start/end session cycle per context
        if (mContextMap.get(context) != null) {
            return;
        }
        mContextMap.put(context, true);


        FlurryAgent.onStartSession(context);
    }

    public synchronized void onEndSession(Context context) {
        // validate parameters
        if (context == null) {
            return;
        }

        // sessions are automatic on ICS+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        }

        // only allow one start/end session cycle per context
        if (mContextMap.get(context) == null) {
            return;
        }
        mContextMap.remove(context);

        FlurryAgent.onEndSession(context);
    }
}

