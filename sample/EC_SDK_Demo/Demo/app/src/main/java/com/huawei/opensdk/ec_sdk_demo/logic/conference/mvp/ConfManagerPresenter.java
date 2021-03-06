package com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.ecterminalsdk.base.TsdkConfRole;
import com.huawei.opensdk.callmgr.CallConstant;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.ec_sdk_demo.R;

import java.util.ArrayList;
import java.util.List;

public class ConfManagerPresenter extends ConfManagerBasePresenter
{
    private static final int ADD_LOCAL_VIEW = 101;

    private boolean is_refresh_view = false;
    private int mCameraIndex = CallConstant.FRONT_CAMERA;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case ADD_LOCAL_VIEW:
                    //setVideoContainer(false);
                    getView().updateLocalVideo();
                    break;

                default:
                    break;
            }
        }
    };

    public ConfManagerPresenter()
    {
        broadcastNames = new String[]{CustomBroadcastConstants.CONF_STATE_UPDATE,
                CustomBroadcastConstants.GET_DATA_CONF_PARAM_RESULT,
                CustomBroadcastConstants.DATA_CONFERENCE_JOIN_RESULT,
                CustomBroadcastConstants.ADD_LOCAL_VIEW,
                CustomBroadcastConstants.DEL_LOCAL_VIEW,
                CustomBroadcastConstants.DATE_CONFERENCE_START_SHARE_STATUS,
                CustomBroadcastConstants.DATE_CONFERENCE_END_SHARE_STATUS,
                CustomBroadcastConstants.REQUEST_CHAIRMAN_RESULT,
                CustomBroadcastConstants.SPEAKER_LIST_IND,
                CustomBroadcastConstants.GET_CONF_END};
    }

    @Override
    protected void onBroadcastReceive(String broadcastName, Object obj)
    {
        switch (broadcastName)
        {
            case CustomBroadcastConstants.CONF_STATE_UPDATE:
                if (!is_refresh_view) {
                    mHandler.sendEmptyMessage(ADD_LOCAL_VIEW);
                    is_refresh_view = true;
                }
                break;

            case CustomBroadcastConstants.ADD_LOCAL_VIEW:
                mHandler.sendEmptyMessage(ADD_LOCAL_VIEW);
                break;

            case CustomBroadcastConstants.DEL_LOCAL_VIEW:
                break;

            default:
                break;
        }
    }

    @Override
    public void switchCamera()
    {
        int callID = MeetingMgr.getInstance().getCurrentConferenceCallID();
        if (callID == 0) {
            return;
        }

        mCameraIndex = CallConstant.FRONT_CAMERA == mCameraIndex ?
                CallConstant.BACK_CAMERA : CallConstant.FRONT_CAMERA;

        CallMgr.getInstance().switchCamera(callID, mCameraIndex);
    }

    @Override
    public void setVideoContainer(Context context, ViewGroup smallLayout, ViewGroup bigLayout, ViewGroup hideLayout)
    {
        //TODO
        //VideoDeviceManager.getInstance().addRenderToContain((FrameLayout) smallLayout, (FrameLayout) bigLayout);
        if (bigLayout != null) {
            addSurfaceView(bigLayout, getRemoteVideoView());
        }

        if (smallLayout != null) {
            addSurfaceView(smallLayout, getLocalVideoView());
        }

        if (hideLayout != null) {
            addSurfaceView(hideLayout, getHideVideoView());
        }
    }

    @Override
    public void setAutoRotation(Object object, boolean isOpen, int orientation) {
        VideoMgr.getInstance().setAutoRotation(object, isOpen, orientation);
    }

    @Override
    public void attachRemoteVideo(long userID, long deviceID)
    {
        //do nothing
    }

    @Override
    public void watchAttendee(Member member) {
        int result = MeetingMgr.getInstance().watchAttendee(member);
        if (0 != result)
        {
            getView().showCustomToast(R.string.watch_conf_fail);
        }
    }

    @Override
    public void setConfMode(ConfConstant.ConfVideoMode confVideoMode) {
        int result = MeetingMgr.getInstance().setConfMode(confVideoMode);
        if (0 != result)
        {
            getView().showCustomToast(R.string.set_mode_failed);
        }
    }

    @Override
    public void shareSelfVideo(long deviceID)
    {
        //do nothing
    }

    @Override
    public void leaveVideo()
    {
        //do nothing
    }

    @Override
    public void changeLocalVideoVisible(boolean visible)
    {
        if (visible) {
            //重新显示本地窗口，无需再打开本地视频
            getLocalVideoView().setVisibility(View.VISIBLE);
        } else {
            //只隐藏本地窗口，并不关闭本地视频
            getLocalVideoView().setVisibility(View.GONE);
        }
    }

    @Override
    public boolean closeOrOpenLocalVideo(boolean close)
    {
        int callID = MeetingMgr.getInstance().getCurrentConferenceCallID();
        if (callID == 0) {
            return false;
        }

        if (close) {
            CallMgr.getInstance().closeCamera(callID);
        } else {
            CallMgr.getInstance().openCamera(callID);
            VideoMgr.getInstance().setVideoOrient(callID, CallConstant.FRONT_CAMERA);
        }

        return true;
    }

    @Override
    public SurfaceView getHideVideoView()
    {
        return VideoMgr.getInstance().getLocalHideView();
    }

    @Override
    public SurfaceView getLocalVideoView()
    {
        return VideoMgr.getInstance().getLocalVideoView();
    }

    @Override
    public SurfaceView getRemoteVideoView()
    {
        return VideoMgr.getInstance().getRemoteVideoView();
    }

    @Override
    public void onItemClick(int position) {
        List<Object> items = new ArrayList<>();
        addLabel(items, position);
        if (!items.isEmpty())
        {
            getView().showItemClickDialog(items, MeetingMgr.getInstance().getCurrentConferenceMemberList().get(position));
        }
    }

    @Override
    public void onItemDetailClick(String clickedItem, Member conferenceMemberEntity) {
        if (LocContext.getString(R.string.watch_contact).equals(clickedItem))
        {
            watchAttendee(conferenceMemberEntity);
        }
        else if (LocContext.getString(R.string.broadcast_mode).equals(clickedItem))
        {
            setConfMode(ConfConstant.ConfVideoMode.CONF_VIDEO_BROADCAST);
        }
        else if (LocContext.getString(R.string.voice_control_mode).equals(clickedItem))
        {
            setConfMode(ConfConstant.ConfVideoMode.CONF_VIDEO_VAS);
        }
        else if (LocContext.getString(R.string.free_mode).equals(clickedItem))
        {
            setConfMode(ConfConstant.ConfVideoMode.CONF_VIDEO_FREE);
        }
    }

    @Override
    public void requestChairman(String chairmanPassword) {
        int result = MeetingMgr.getInstance().requestChairman(chairmanPassword);
        if (result != 0) {
            getView().showCustomToast(R.string.request_chairman_fail);
            return;
        }
    }

    @Override
    public void releaseChairman() {
        int result = MeetingMgr.getInstance().releaseChairman();
        if (result != 0) {
            getView().showCustomToast(R.string.release_chairman_fail);
            return;
        }
    }

    @Override
    public void muteConf(boolean isMute) {
        int result = MeetingMgr.getInstance().muteConf(isMute);
        if (result != 0)
        {
            if (isMute) {
                getView().showCustomToast(R.string.mute_conf_fail);
            } else {
                getView().showCustomToast(R.string.un_mute_conf_fail);
            }
        }
    }

    @Override
    public void lockConf(boolean isLock) {
        int result = MeetingMgr.getInstance().lockConf(isLock);
        if (result != 0)
        {
            if (isLock) {
                getView().showCustomToast(R.string.lock_conf_fail);
            } else {
                getView().showCustomToast(R.string.un_lock_conf_fail);
            }
        }
    }

    @Override
    public void handUpSelf() {
        Member self = getSelf();
        if (self == null)
        {
            return;
        }

        boolean isHandUp = !self.isHandUp();
        int result = MeetingMgr.getInstance().handup(isHandUp, self);
        if (result != 0)
        {
            if (isHandUp) {
                getView().showCustomToast(R.string.handup_fail);
            } else {
                getView().showCustomToast(R.string.cancel_handup_fail);
            }
        }
    }

    @Override
    public boolean isHandUp() {
        Member self = getSelf();
        if (self == null)
        {
            return false;
        }
        return self.isHandUp();
    }

    @Override
    public boolean isConfMute() {
        ConfBaseInfo confBaseInfo = MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
        if (confBaseInfo == null)
        {
            return false;
        }
        return confBaseInfo.isMuteAll();
    }

    @Override
    public boolean isConfLock() {
        ConfBaseInfo confBaseInfo = MeetingMgr.getInstance().getCurrentConferenceBaseInfo();
        if (confBaseInfo == null)
        {
            return false;
        }
        return confBaseInfo.isLock() ;
    }

    @Override
    public void updateConf() {
        int result = MeetingMgr.getInstance().upgradeConf();
        if (result != 0)
        {
            getView().showCustomToast(R.string.upgrade_conf_fail);
            return;
        }
    }

    @Override
    public void addMember(String name, String number, String account) {
        Member member = new Member();
        member.setNumber(number);
        member.setDisplayName(name);
        member.setAccountId(account);
        member.setRole(TsdkConfRole.TSDK_E_CONF_ROLE_ATTENDEE);

        int result = MeetingMgr.getInstance().addAttendee(member);
        if (result != 0)
        {
            getView().showCustomToast(R.string.add_attendee_fail);
        }
    }

    private Member getSelf()
    {
        return MeetingMgr.getInstance().getCurrentConferenceSelf();
    }

    private void addSurfaceView(ViewGroup container, SurfaceView child)
    {
        if (child == null)
        {
            return;
        }
        if (child.getParent() != null)
        {
            ViewGroup vGroup = (ViewGroup) child.getParent();
            vGroup.removeAllViews();
        }
        container.addView(child);
    }

    private void addLabel(List<Object> items, int position)
    {
        Member member = MeetingMgr.getInstance().getCurrentConferenceMemberList().get(position);

        switch (member.getStatus())
        {
            case IN_CONF:
                if (isChairMan())
                {
                    items.add(LocContext.getString(R.string.broadcast_contact));
                    items.add(LocContext.getString(R.string.cancel_broadcast_contact));
                    items.add(LocContext.getString(R.string.watch_contact));
                }
                else
                {
                    items.add(LocContext.getString(R.string.watch_contact));
                }
                break;

            default:
                break;
        }
    }

}
