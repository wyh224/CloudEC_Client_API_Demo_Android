package com.huawei.opensdk.ec_sdk_demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.huawei.common.res.LocContext;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.ctdservice.CtdMgr;
import com.huawei.opensdk.commonservice.util.CrashUtil;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.contactservice.eaddr.EnterpriseAddressBookMgr;
import com.huawei.opensdk.demoservice.ConfConvertUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.ec_sdk_demo.common.UIConstants;
import com.huawei.opensdk.ec_sdk_demo.logic.call.CallFunc;
import com.huawei.opensdk.ec_sdk_demo.logic.conference.ConfFunc;
import com.huawei.opensdk.ec_sdk_demo.logic.eaddrbook.EnterpriseAddrBookFunc;
import com.huawei.opensdk.ec_sdk_demo.logic.im.ImFunc;
import com.huawei.opensdk.ec_sdk_demo.logic.login.LoginFunc;
import com.huawei.opensdk.ec_sdk_demo.logic.login.LoginModel;
import com.huawei.opensdk.ec_sdk_demo.util.FileUtil;
import com.huawei.opensdk.ec_sdk_demo.util.ZipUtil;
import com.huawei.opensdk.imservice.ImMgr;
import com.huawei.opensdk.loginmgr.LoginConstant;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.huawei.opensdk.servicemgr.ServiceMgr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//import com.huawei.opensdk.commonservice.common.LocContext;

public class ECApplication extends Application
{
    private static final int EXPECTED_FILE_LENGTH = 7;

    private static final String FRONT_PKG = "com.huawei.opensdk.ec_sdk_demo";

    private int mIdoProtocol = 0;
    private SharedPreferences mSharedPreferences;
    private LoginModel mSettingPresenter;

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (!isFrontProcess(this,FRONT_PKG))
        {
            LocContext.init(this);
            CrashUtil.getInstance().init(this);
            Log.i("SDKDemo", "onCreate: PUSH Process.");
            return;
        }
        initConfCtrlProtocol();
        String appPath = getApplicationInfo().dataDir + "/lib";
        ServiceMgr.getServiceMgr().startService(this, appPath, mIdoProtocol);
        Log.i("SDKDemo", "onCreate: MAIN Process.");

        LoginMgr.getInstance().regLoginEventNotification(LoginFunc.getInstance());
        CallMgr.getInstance().regCallServiceNotification(CallFunc.getInstance());
        CtdMgr.getInstance().regCtdNotification(CallFunc.getInstance());
        MeetingMgr.getInstance().regConfServiceNotification(ConfFunc.getInstance());
        ImMgr.getInstance().regImServiceNotification(ImFunc.getInstance());
        EnterpriseAddressBookMgr.getInstance().registerNotification(EnterpriseAddrBookFunc.getInstance());

        ServiceMgr.getServiceMgr().securityParam(
                mSettingPresenter.getSrtpMode(),
                mSettingPresenter.getSipTransport(),
                mSettingPresenter.getAppConfig(),
                mSettingPresenter.getTunnelMode());
        ServiceMgr.getServiceMgr().networkParam(
                mSettingPresenter.getUdpPort(),
                mSettingPresenter.getTlsPort(),
                mSettingPresenter.getPriority());

        initResourceFile();
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    }

    private void initConfCtrlProtocol()
    {
        mSharedPreferences = getSharedPreferences(LoginConstant.FILE_NAME, Activity.MODE_PRIVATE);
        mSettingPresenter = new LoginModel(mSharedPreferences);
        mIdoProtocol = mSharedPreferences.getInt(LoginConstant.CONF_CTRL_PROTOCOL, 0);
        MeetingMgr.getInstance().setConfProtocol(ConfConvertUtil.convertConfctrlProtocol(mIdoProtocol));
    }

    private void initResourceFile()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                initDataConfRes();
            }
        }).start();
    }

    private void initDataConfRes()
    {
        String path = LocContext.
                getContext().getFilesDir() + "/AnnoRes";
        File file = new File(path);
        if (file.exists())
        {
            LogUtil.i(UIConstants.DEMO_TAG,  file.getAbsolutePath());
            File[] files = file.listFiles();
            if (null != files && EXPECTED_FILE_LENGTH == files.length)
            {
                return;
            }
            else
            {
                FileUtil.deleteFile(file);
            }
        }

        try
        {
            InputStream inputStream = getAssets().open("AnnoRes.zip");
            ZipUtil.unZipFile(inputStream, path);
        }
        catch (IOException e)
        {
            LogUtil.i(UIConstants.DEMO_TAG,  "close...Exception->e" + e.toString());
        }
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();

        ServiceMgr.getServiceMgr().stopService();
    }

    private static boolean isFrontProcess(Context context, String frontPkg)
    {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        if (infos == null || infos.isEmpty())
        {
            return false;
        }

        final int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : infos)
        {
            if (info.pid == pid)
            {
                Log.i(UIConstants.DEMO_TAG, "processName-->"+info.processName);
                return frontPkg.equals(info.processName);
            }
        }

        return false;
    }
}
