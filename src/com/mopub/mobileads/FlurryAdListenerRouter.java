package com.mopub.mobileads;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdType;

public class FlurryAdListenerRouter implements FlurryAdListener {
    public static final String kLogTag = FlurryAdListenerRouter.class.getSimpleName();

    private static FlurryAdListenerRouter sRouter;

    public static synchronized FlurryAdListenerRouter getInstance() {
        if (sRouter == null) {
            sRouter = new FlurryAdListenerRouter();
        }

        return sRouter;
    }

    private final Map<String, FlurryAdListener> mAdSpaceToListenerMap = new HashMap<String, FlurryAdListener>();

    private FlurryAdListenerRouter() {
    }

    public void register(String adSpace, FlurryAdListener listener) {
        mAdSpaceToListenerMap.put(adSpace, listener);
    }

    public void unregister(String adSpace) {
        mAdSpaceToListenerMap.remove(adSpace);
    }

    @Override
    public void onAdClicked(String adSpace) {
        Log.d(kLogTag, "onAdClicked (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onAdClicked(adSpace);
    }

    @Override
    public void onAdClosed(String adSpace) {
        Log.d(kLogTag, "onAdClosed (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onAdClosed(adSpace);
    }

    @Override
    public void onAdOpened(String adSpace) {
        Log.d(kLogTag, "onAdOpened (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onAdOpened(adSpace);
    }

    @Override
    public void onApplicationExit(String adSpace) {
        Log.d(kLogTag, "onApplicationExit (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onApplicationExit(adSpace);
    }

    @Override
    public void onRendered(String adSpace) {
        Log.d(kLogTag, "onRendered (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onRendered(adSpace);
    }

    @Override
    public void onRenderFailed(String adSpace) {
        Log.d(kLogTag, "onRenderFailed (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onRenderFailed(adSpace);
    }

    @Override
    public void onVideoCompleted(String adSpace) {
        Log.d(kLogTag, "onVideoCompleted (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).onVideoCompleted(adSpace);
    }

    @Override
    public boolean shouldDisplayAd(String adSpace, FlurryAdType arg1) {
        Log.d(kLogTag, "shouldDisplayAd (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).shouldDisplayAd(adSpace, arg1);
        return true;
    }

    @Override
    public void spaceDidFailToReceiveAd(String adSpace) {
        Log.d(kLogTag, "spaceDidFailToReceiveAd (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).spaceDidFailToReceiveAd(adSpace);
    }

    @Override
    public void spaceDidReceiveAd(String adSpace) {
        Log.d(kLogTag, "spaceDidReceiveAd (" + adSpace + ") ");
        mAdSpaceToListenerMap.get(adSpace).spaceDidReceiveAd(adSpace);
    }
}
