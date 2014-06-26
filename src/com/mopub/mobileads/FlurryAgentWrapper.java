package com.mopub.mobileads;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;

import java.util.WeakHashMap;

public class FlurryAgentWrapper {
    private static FlurryAgentWrapper sWrapper;

    public static synchronized FlurryAgentWrapper getInstance() {
        if (sWrapper == null) {
            sWrapper = new FlurryAgentWrapper();
        }

        return sWrapper;
    }

    private final WeakHashMap<Context, Boolean> mContextMap = new WeakHashMap<Context, Boolean>();

    private boolean mLifecycleListenerRegistered;
    private String mApiKey;

    private FlurryAgentWrapper() {
        FlurryAgent.setLogEnabled(false);
        FlurryAgent.setLogLevel(Log.INFO);
        FlurryAgent.addOrigin("Flurry_Mopub_Android", "4.0.0");

        FlurryAds.enableTestAds(false);
        FlurryAds.setAdListener(FlurryAdListenerRouter.getInstance());
    }

    public synchronized void onStartSession(Context context, String apiKey) {
        // validate parameters
        if (context == null || apiKey == null || apiKey.length() == 0) {
            return;
        }

        // register lifecycle provider only once
        if (!mLifecycleListenerRegistered) {
            mLifecycleListenerRegistered = registerLifecycleListener(context);
        }

        // only allow one start/end session cycle per context
        if (mContextMap.get(context) != null) {
            return;
        }
        mContextMap.put(context, true);

        // ensure we are only using one API key per app
        if (mApiKey != null && mApiKey.length() > 0) {
            if (!mApiKey.equals(apiKey)) {
                throw new IllegalStateException("Only one API key per application is supported");
            }
        } else {
            mApiKey = apiKey;
        }

        FlurryAgent.onStartSession(context, apiKey);
    }

    public synchronized void onEndSession(Context context) {
        onEndSession(context, false);
    }

    private synchronized void onEndSession(Context context, boolean lifecycleListener) {
        // validate parameters
        if (context == null) {
            return;
        }

        // use the lifecycle listener to end the session, if available
        if (mLifecycleListenerRegistered && !lifecycleListener) {
            return;
        }

        // only allow one start/end session cycle per context
        if (mContextMap.get(context) == null) {
            return;
        }
        mContextMap.remove(context);

        FlurryAgent.onEndSession(context);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private synchronized boolean registerLifecycleListener(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (context == null) {
                return false;
            }

            Application application = (Application) context.getApplicationContext();
            if (application == null) {
                return false;
            }

            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    // ignore
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    // ignore
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    // ignore
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    // ignore
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    onEndSession(activity, true);
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    // ignore
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    // ignore
                }
            });

            return true;
        }

        return false;
    }
}
