package com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp;

import android.content.Context;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.huawei.opensdk.demoservice.ConfBaseInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.ec_sdk_demo.logic.BaseView;

import java.util.List;


public interface IConfManagerContract
{
    interface ConfManagerView extends BaseView
    {
        void finishActivity();

        void updateMuteButton(boolean isMute);

        void updateAttendeeButton(Member member);

        void updateLocalVideo();

        void refreshMemberList(final List<Member> list);

        void showItemClickDialog(List<Object> items, Member member);

        void updateUpgradeConfBtn(boolean isInDataConf);

        void updateConfTypeIcon(ConfBaseInfo confBaseInfo);

        void showMessage(String message);

        void startAsShare(boolean isShare);
    }

    interface ConfManagerPresenter
    {
        void registerBroadcast();

        void unregisterBroadcast();

        void setConfID(String confID);

        boolean muteSelf();

        int switchLoudSpeaker();

        void switchCamera();

        boolean isChairMan();

        void setVideoContainer(Context context, ViewGroup smallLayout, ViewGroup bigLayout, ViewGroup hideLayout);

        void setAutoRotation(Object object, boolean isOpen, int orientation);

        /**
         * 打开指定与会者的视频
         * @param userID
         */
        void attachRemoteVideo(long userID, long deviceID);

        void watchAttendee(Member member);

        void setConfMode(ConfConstant.ConfVideoMode confVideoMode);

        /**
         * 共享自己的视频
         */
        void shareSelfVideo(long deviceID);

        void closeConf();

        void finishConf();

        void leaveVideo();

        List<Member> getMemberList();

        void changeLocalVideoVisible(boolean visible);

        boolean closeOrOpenLocalVideo(boolean close);

        SurfaceView getHideVideoView();

        SurfaceView getLocalVideoView();

        SurfaceView getRemoteVideoView();

        void onItemClick(int position);

        void onItemDetailClick(String clickedItem, Member conferenceMemberEntity);

        void requestChairman(String chairmanPassword);

        void releaseChairman();

        void muteConf(boolean isMute);

        void lockConf(boolean isLock);

        void handUpSelf();

        boolean isHandUp();

        boolean isConfMute();

        boolean isConfLock();

        void updateConf();

        void addMember(String name, String number, String account);

        void setSelfPresenter();

    }
}
