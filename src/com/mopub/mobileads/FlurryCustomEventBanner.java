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

public class FlurryCustomEventBanner extends com.mopub.mobileads.CustomEventBanner implements FlurryAdListener {
    public static final String LOG_TAG = FlurryCustomEventBanner.class.getSimpleName();

    private static final String API_KEY = "apiKey";
    private static final String AD_SPACE_NAME = "adSpaceName";

    private Context mContext;
    private CustomEventBannerListener mListener;
    private FrameLayout mLayout;

    private String mApiKey;
    private String mAdSpaceName;

    public FlurryCustomEventBanner() {
        super();
    }

    // CustomEventBanner
    @Override
    protected void loadBanner(Context context,
                              CustomEventBannerListener listener,
                              Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null!");
        }

        if (listener == null) {
            throw new IllegalArgumentException("CustomEventBannerListener cannot be null!");
        }

        if (!(context instanceof Activity)) {
            listener.onBannerFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (!extrasAreValid(serverExtras)) {
            listener.onBannerFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        mContext = context;
        mListener = listener;
        mLayout = new FrameLayout(context);

        mApiKey = serverExtras.get(API_KEY);
        mAdSpaceName = serverExtras.get(AD_SPACE_NAME);

        FlurryAgentWrapper.getInstance().onStartSession(context, mApiKey);
        FlurryAdListenerRouter.getInstance().register(mAdSpaceName, this);

        Log.d(LOG_TAG, "fetch Flurry Ad (" + mAdSpaceName + ") -- " + mLayout.toString());
        FlurryAds.fetchAd(mContext, mAdSpaceName, mLayout, FlurryAdSize.BANNER_TOP);
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
    public void spaceDidFailToReceiveAd(String adSpace) {
        Log.d(LOG_TAG, "Flurry space did fail to receive ad (" + adSpace + ")");
        mListener.onBannerFailed(NETWORK_NO_FILL);
    }

    @Override
    public void spaceDidReceiveAd(String adSpace) {
        Log.d(LOG_TAG, "Flurry space did receive ad (" + adSpace + ")");
        FlurryAds.displayAd(mContext, adSpace, mLayout);
    }

    @Override
    public boolean shouldDisplayAd(String adSpace, FlurryAdType type) {
        Log.d(LOG_TAG, "Flurry should display ad (" + adSpace + ")");
        return true;
    }

    @Override
    public void onAdClicked(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad clicked (" + adSpace + ")");
        mListener.onBannerClicked();
    }

    @Override
    public void onAdClosed(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad closed (" + adSpace + ")");
        mListener.onBannerCollapsed();
    }

    @Override
    public void onAdOpened(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad opened (" + adSpace + ")");
        mListener.onBannerExpanded();
    }

    @Override
    public void onApplicationExit(String adSpace) {
        Log.d(LOG_TAG, "onApplicationExit (" + adSpace + ")");
        mListener.onLeaveApplication();
    }

    @Override
    public void onRendered(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad rendered (" + adSpace + ")");
        mListener.onBannerLoaded(mLayout);
    }

    @Override
    public void onRenderFailed(String adSpace) {
        Log.d(LOG_TAG, "Flurry ad render failed (" + adSpace + ")");
        mListener.onBannerFailed(NETWORK_NO_FILL);
    }

    @Override
    public void onVideoCompleted(String adSpace) {
        Log.d(LOG_TAG, "Flurry video completed (" + adSpace + ")");
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        if (serverExtras == null) {
            return false;
        }

        return serverExtras.containsKey(API_KEY) && serverExtras.containsKey(AD_SPACE_NAME);
    }
}
