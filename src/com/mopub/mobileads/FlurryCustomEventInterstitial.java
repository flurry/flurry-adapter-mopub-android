package com.mopub.mobileads;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdListener;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;

public class FlurryCustomEventInterstitial extends com.mopub.mobileads.CustomEventInterstitial implements FlurryAdListener {
    public static final String LOG_TAG = FlurryCustomEventInterstitial.class.getSimpleName();

    private static final String API_KEY = "apiKey";
    private static final String AD_SPACE_NAME = "adSpaceName";

    private Context mContext;
    private CustomEventInterstitialListener mListener;
    private FrameLayout mLayout;

    private String mApiKey;
    private String mAdSpaceName;

    public FlurryCustomEventInterstitial() {
        super();
    }

    // CustomEventInterstitial
    @Override
    protected void loadInterstitial(Context context,
                                    CustomEventInterstitialListener listener,
                                    Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null!");
        }

        if (listener == null) {
            throw new IllegalArgumentException("CustomEventInterstitialListener cannot be null!");
        }

        if (!(context instanceof Activity)) {
            listener.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (!extrasAreValid(serverExtras)) {
            listener.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        mContext = context;
        mListener = listener;
        mLayout = new FrameLayout(context);

        mApiKey = serverExtras.get(API_KEY);
        mAdSpaceName = serverExtras.get(AD_SPACE_NAME);

        FlurryAgentWrapper.getInstance().onStartSession(context, mApiKey);
        FlurryAdListenerRouter.getInstance().register(mAdSpaceName, this);

        Log.d(LOG_TAG, "fetch Flurry ad (" + mAdSpaceName + ") -- " + mLayout.toString());
        FlurryAds.fetchAd(mContext, mAdSpaceName, mLayout, FlurryAdSize.FULLSCREEN);
    }

    @Override
    protected void onInvalidate() {
        if (mContext == null) {
            return;
        }

        Log.d(LOG_TAG, "MoPub issued onInvalidate (" + mAdSpaceName + ")");

        FlurryAds.removeAd(mContext, mAdSpaceName, mLayout);

        FlurryAdListenerRouter.getInstance().unregister(mAdSpaceName);
        FlurryAgentWrapper.getInstance().onEndSession(mContext);

        mContext = null;
        mListener = null;
        mLayout = null;
    }

    // FlurryAdListener
    @Override
    protected void showInterstitial() {
        FlurryAds.displayAd(mContext, mAdSpaceName, mLayout);
    }

    //FlurryAdListener callbacks
    @Override
    public void spaceDidReceiveAd(String adSpace) {
        Log.d(LOG_TAG, "Flurry space did receive ad (" + adSpace + ")");
        mListener.onInterstitialLoaded();
    }

    @Override
    public void onAdClicked(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad clicked (" + adSpace + ")");
        mListener.onInterstitialClicked();
    }

    @Override
    public void onAdClosed(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad closed (" + adSpace + ")");
        mListener.onInterstitialDismissed();
    }

    @Override
    public void onAdOpened(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad opened (" + adSpace + ")");
    }

    @Override
    public void onApplicationExit(String adSpace) {
        Log.d(LOG_TAG, "onApplicationExit (" + adSpace + ")");
        mListener.onLeaveApplication();
    }

    @Override
    public void onRendered(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad rendered (" + adSpace + ")");
        mListener.onInterstitialShown();
    }

    @Override
    public void onRenderFailed(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad render failed (" + adSpace + ")");
        mListener.onInterstitialFailed(NETWORK_INVALID_STATE);
    }

    @Override
    public void onVideoCompleted(String adSpace) {
        Log.d(LOG_TAG, "Flurry video completed (" + adSpace + ")");
    }

    @Override
    public boolean shouldDisplayAd(String adSpace, FlurryAdType adType) {
        Log.d(LOG_TAG, "Flurry should display ad (" + adSpace + ")");
        return true;
    }

    @Override
    public void spaceDidFailToReceiveAd(String adSpace) {
        Log.d(LOG_TAG, "Flurry space did fail to receive ad (" + adSpace + ")");
        mListener.onInterstitialFailed(NETWORK_NO_FILL);
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        if (serverExtras == null) {
            return false;
        }

        return serverExtras.containsKey(API_KEY) && serverExtras.containsKey(AD_SPACE_NAME);
    }
}
