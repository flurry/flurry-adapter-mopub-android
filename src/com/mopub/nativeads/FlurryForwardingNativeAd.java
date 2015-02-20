
package com.mopub.nativeads;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdNative;
import com.flurry.android.ads.FlurryAdNativeAsset;
import com.flurry.android.ads.FlurryAdNativeListener;
import com.mopub.mobileads.FlurryAgentWrapper;

import java.util.ArrayList;
import java.util.List;


public class FlurryForwardingNativeAd extends BaseForwardingNativeAd{

    private static final String kLogTag = FlurryForwardingNativeAd.class.getSimpleName();
    private static final int IMPRESSION_VIEW_MIN_TIME = 1000;

    private final Context mContext;
    private final CustomEventNative.CustomEventNativeListener mCustomEventNativeListener;
    private final FlurryForwardingNativeAd mFlurryForwardingNativeAd;

    private FlurryAdNative nativeAd;

    FlurryForwardingNativeAd(Context context, FlurryAdNative adNative, CustomEventNative.CustomEventNativeListener mCustomEventNativeListener) {
        this.mContext = context;
        this.nativeAd = adNative;
        this.mCustomEventNativeListener = mCustomEventNativeListener;
        this.mFlurryForwardingNativeAd = this;
    }

    public synchronized void fetchAd() {
        Context context = mContext;

        if (context != null) {
            Log.i(kLogTag, "Fetching Native Ad now");
            nativeAd.setListener(listener);
            nativeAd.fetchAd();
        } else {
            Log.i(kLogTag, "Context is null, not fetching Native Ad");
        }
    }


    private synchronized void onFetched(FlurryAdNative adNative) {
        if (adNative != null) {
            Log.i(kLogTag, "onFetched: Native Ad fetched successfully!");
            setupNativeAd(adNative);
        }
    }

    private synchronized void onFetchFailed(FlurryAdNative adNative) {
        Log.i(kLogTag, "onFetchFailed: Native ad not available.");
        if(mCustomEventNativeListener != null)
            mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL);
    }



    private synchronized void setupNativeAd(FlurryAdNative adNative){
        if (adNative != null) {

            nativeAd = adNative;
            FlurryAdNativeAsset coverImageAsset = nativeAd.getAsset("secHqImage");
            FlurryAdNativeAsset iconImageAsset = nativeAd.getAsset("secImage");

            if (coverImageAsset != null && !TextUtils.isEmpty(coverImageAsset.getValue())) {
                setMainImageUrl(coverImageAsset.getValue());
            }
            if (iconImageAsset != null && !TextUtils.isEmpty(iconImageAsset.getValue())) {
                setIconImageUrl(iconImageAsset.getValue());
            }

            setTitle(nativeAd.getAsset("headline").getValue());
            setText(nativeAd.getAsset("summary").getValue());

            //setCallToAction(CALL_TO_ACTION);
            setImpressionMinTimeViewed(IMPRESSION_VIEW_MIN_TIME);
            setOverridingClickTracker(true);
            setOverridingImpressionTracker(true);

            preCacheImages(mContext, getImageUrls(), new CustomEventNative.ImageListener() {

                @Override
                public void onImagesCached() {
                    if(mCustomEventNativeListener != null)
                        Log.i(kLogTag, "preCacheImages: Ad image cached.");
                        mCustomEventNativeListener.onNativeAdLoaded(mFlurryForwardingNativeAd);
                }

                @Override
                public void onImagesFailedToCache(NativeErrorCode errorCode) {
                    if(mCustomEventNativeListener != null) {
                        Log.i(kLogTag, "preCacheImages: Unable to cache Ad image.");
                        mCustomEventNativeListener.onNativeAdFailed(errorCode);
                    }
                }
            });
        }

    }

    private List<String> getImageUrls() {
        final List<String> imageUrls = new ArrayList<String>(2);
        final String mainImageUrl = getMainImageUrl();

        if (mainImageUrl != null) {
            imageUrls.add(getMainImageUrl());
        }

        final String iconUrl = getIconImageUrl();
        if (iconUrl != null) {
            imageUrls.add(this.getIconImageUrl());
        }
        return imageUrls;
    }


    // BaseForwardingNativeAd
    @Override
    public void prepare(final View view) {
        nativeAd.setTrackingView(view);
    }

    @Override
    public void clear(@Nullable View view) {
        super.clear(view);
        nativeAd.removeTrackingView();
    }

    @Override
    public void destroy() {
        nativeAd.destroy();
        FlurryAgentWrapper.getInstance().onEndSession(mContext);
    }

    FlurryAdNativeListener listener = new FlurryAdNativeListener() {
        @Override
        public void onFetched(FlurryAdNative adNative) {
            Log.i(kLogTag, "onFetched: Successful.");
            mFlurryForwardingNativeAd.onFetched(adNative);
        }

        @Override
        public void onError(FlurryAdNative adNative, FlurryAdErrorType adErrorType, int errorCode) {
            if (adErrorType.equals(FlurryAdErrorType.FETCH)) {
                Log.i(kLogTag, "onFetchFailed: Error code: " + errorCode);
                mFlurryForwardingNativeAd.onFetchFailed(adNative);
            }
        }

        @Override
        public void onShowFullscreen(FlurryAdNative adNative) {}

        @Override
        public void onCloseFullscreen(FlurryAdNative adNative) {}

        @Override
        public void onClicked(FlurryAdNative adNative) {
            Log.i(kLogTag, "onClicked: Successful.");
            notifyAdClicked();
        }

        @Override
        public void onImpressionLogged(FlurryAdNative flurryAdNative) {
            Log.i(kLogTag, "onImpressionLogged: Successful.");
            notifyAdImpressed();
        }

        @Override
        public void onAppExit(FlurryAdNative adNative) {}


    };
}
