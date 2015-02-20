
package com.mopub.nativeads;

import android.content.Context;
import android.util.Log;

import com.flurry.android.ads.FlurryAdNative;
import com.mopub.mobileads.FlurryAgentWrapper;

import java.util.Map;


public class FlurryCustomEventNative extends CustomEventNative {

    private static final String kLogTag = FlurryCustomEventNative.class.getSimpleName();
    private static final String FLURRY_APIKEY = "apiKey";
    private static final String FLURRY_ADSPACE = "adSpaceName";

    @Override
    protected void loadNativeAd(final Context context,
                                final CustomEventNativeListener customEventNativeListener,
                                final Map<String, Object> localExtras,
                                final Map<String, String> serverExtras) {


        //Get the API KEY & AD SPACE from the server.
        final String flurryApiKey;
        final String flurryAdSpace;
        if (validateExtras(serverExtras)) {
            flurryApiKey = serverExtras.get(FLURRY_APIKEY);
            flurryAdSpace = serverExtras.get(FLURRY_ADSPACE);
            FlurryAgentWrapper.getInstance().onStartSession(context, flurryApiKey);
        } else {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            Log.i(kLogTag, "Failed Native AdFetch: Missing required server extras [FLURRY_APIKEY and/or FLURRY_ADSPACE].");
            return;
        }

        final FlurryForwardingNativeAd mflurryForwardingNativeAd =
                new FlurryForwardingNativeAd(context, new FlurryAdNative(context, flurryAdSpace), customEventNativeListener);
        mflurryForwardingNativeAd.fetchAd();

    }

    private boolean validateExtras(final Map<String, String> serverExtras) {
        final String flurryApiKey = serverExtras.get(FLURRY_APIKEY);
        final String flurryAdSpace = serverExtras.get(FLURRY_ADSPACE);
        Log.i(kLogTag, "ServerInfo fetched from Mopub " + FLURRY_APIKEY + " : "+ flurryApiKey + " and " + FLURRY_ADSPACE  + " :" +flurryAdSpace);
        return ((flurryApiKey != null && flurryApiKey.length() > 0) && (flurryAdSpace != null && flurryAdSpace.length() > 0));
    }

}
