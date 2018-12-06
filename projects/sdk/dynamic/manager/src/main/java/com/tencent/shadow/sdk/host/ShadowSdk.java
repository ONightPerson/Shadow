package com.tencent.shadow.sdk.host;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.tencent.shadow.sdk.pluginmanager.ViewCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ShadowSdk {

    private static final String KEY_APP_TYPE = "KEY_APP_TYPE";

    private static final String KEY_FROM_ID = "KEY_FROM_ID";

    private static final String KEY_BUNDLE_DATA = "KEY_BUNDLE_DATA";

    private static final String INTENT_ACTION = "com.tencent.shadow.sdk.action.enter";

    private static Map<String, UpgradeablePluginManager> sUPMs = new HashMap<>();


    private static boolean isInit;

    /**
     * 下载PluginManager超时时间
     */
    private static final long TIME_OUT = 3000;


    public static void enter(Context context, long formId, Bundle bundle, ViewCallback viewCallback) throws Exception {
        initReceiverIfNeeded(context);
        UpgradeablePluginManager upgradeablePluginManager = getUpgradeablePluginManager(context, String.valueOf(formId));
        upgradeablePluginManager.upgradeIfNeededThenInit(TIME_OUT, TimeUnit.MILLISECONDS);
        upgradeablePluginManager.enter(context, formId, bundle, viewCallback);
    }

    private static UpgradeablePluginManager getUpgradeablePluginManager(Context context, String appType) {
        UpgradeablePluginManager upgradeablePluginManager = sUPMs.get(appType);
        if (upgradeablePluginManager == null) {
            upgradeablePluginManager = new UpgradeablePluginManager(context.getFilesDir(), appType);
            sUPMs.put(appType, upgradeablePluginManager);
        }
        return upgradeablePluginManager;
    }

    private static void initReceiverIfNeeded(Context context) {
        if (!isInit) {
            IntentFilter intentFilter = new IntentFilter(INTENT_ACTION);
            context.getApplicationContext().registerReceiver(sBroadcastReceiver, intentFilter);
            isInit = true;
        }
    }

    private static BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String appType = intent.getStringExtra(KEY_APP_TYPE);
            long fromId = intent.getLongExtra(KEY_FROM_ID, 0);
            Bundle bundle = intent.getBundleExtra(KEY_BUNDLE_DATA);
            try {
                ShadowSdk.enter(context, fromId, bundle, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


}
