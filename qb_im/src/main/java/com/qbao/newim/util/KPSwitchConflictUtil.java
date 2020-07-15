package com.qbao.newim.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.MotionEvent;
import android.view.View;

import com.qbao.newim.activity.NIMChatActivity;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/4/6.
 */

public class KPSwitchConflictUtil {
    public static void attach(final View panelLayout,
                              final View focusView,
                              final SwitchClickListener switchClickListener,
                              SubPanelAndTrigger... subPanelAndTriggers) {
        final Activity activity = (Activity) panelLayout.getContext();

        for (SubPanelAndTrigger subPanelAndTrigger : subPanelAndTriggers) {

            bindSubPanel(subPanelAndTrigger, subPanelAndTriggers,
                    focusView, panelLayout, switchClickListener);
        }

        if (isHandleByPlaceholder(activity)) {
            focusView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        panelLayout.setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
        }
    }

    private static void bindSubPanel(final SubPanelAndTrigger subPanelAndTrigger,
                                     final SubPanelAndTrigger[] subPanelAndTriggers,
                                     final View focusView, final View panelLayout,
                                     final SwitchClickListener switchClickListener) {

        // 切换按钮
        final View triggerView = subPanelAndTrigger.triggerView;

        /**
         * type=0时，该view是表情面板和多媒体面板
         * type=1时，该view是录制语音界面
         */
        final View boundTriggerSubPanelView = subPanelAndTrigger.subPanelView;
        final int nType = subPanelAndTrigger.nType;

        triggerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.chat_add_audio) {
                    Context context = v.getContext();
                    NIMChatActivity activity = null;
                    while (context instanceof ContextWrapper) {
                        if (context instanceof Activity) {
                            activity =  (NIMChatActivity) context;
                        }
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (activity != null) {
                        activity.requestAudioPermission();
                    }
                }
                Boolean switchToPanel = null;

                if (nType > 0) {

                    /**
                     * 第一：录音布局可见-->
                     *          a, 如果当前录音布局可见，点击隐藏语音布局，编辑框可见，显示键盘，切换按钮样式为语音录制
                     *
                     * 第二：录音布局不可见分三种-->
                     *          b，如果当前面板可见，点击语音，隐藏键盘和面板，弹出录音布局，隐藏编辑框，切换按钮样式为键盘打开
                     *          c，如果当前面板不可见，键盘也不可见，直接把编辑框隐藏，弹出录音布局即可，切换按钮样式为键盘打开
                     *          d，如果当前键盘可见，隐藏键盘，弹出录音布局，切换按钮样式为键盘打开
                     */
                    if (boundTriggerSubPanelView.getVisibility() == View.VISIBLE) {
                        boundTriggerSubPanelView.setVisibility(View.GONE);
                        focusView.setVisibility(View.VISIBLE);
                        KeyboardUtil.showKeyboard(focusView);
                        triggerView.setBackgroundResource(R.drawable.nim_chat_add_audio_selector);
                        switchToPanel = false;
                    } else {
                        hidePanelAndKeyboard(panelLayout);
                        triggerView.setBackgroundResource(R.drawable.nim_chat_open_keybord_selector);
                        focusView.setVisibility(View.GONE);
                        boundTriggerSubPanelView.setVisibility(View.VISIBLE);
                    }

                } else {
                    /**
                     * 1，当前面板可见，点击分两种，一判断当前需要显示的已可见了，显示键盘
                     *                            二当前需要显示的不可见，显示
                     *          面板可见情况下，输入框布局一定是可见的
                     * 2，当前面板不可见，此时如果输入框不可见，即语音布局可见，需要隐藏语音，显示输入框，并显示面板布局
                     *                   此时如果输入框可见，直接显示面板即可
                     */
                    if (panelLayout.getVisibility() == View.VISIBLE) {
                        if (boundTriggerSubPanelView.getVisibility() == View.VISIBLE) {
                            KPSwitchConflictUtil.showKeyboard(panelLayout, focusView);
                            switchToPanel = false;
                        } else {
                            showBoundTriggerSubPanel(boundTriggerSubPanelView, subPanelAndTriggers);
                            switchToPanel = false;
                        }
                    } else {
                        if (focusView.getVisibility() == View.GONE) {
                            hideAudioLayout(subPanelAndTriggers);
                            focusView.setVisibility(View.VISIBLE);
                        }
                        KPSwitchConflictUtil.showPanel(panelLayout);
                        switchToPanel = true;
                        showBoundTriggerSubPanel(boundTriggerSubPanelView, subPanelAndTriggers);
                    }
                }

                if (switchClickListener != null && switchToPanel != null) {
                    switchClickListener.onClickSwitch(switchToPanel);
                }

            }
        });
    }

    /**
     * 显示面板
     * @param panelLayout 需要显示的面板
     */
    public static void showPanel(final View panelLayout) {
        final Activity activity = (Activity) panelLayout.getContext();
        panelLayout.setVisibility(View.VISIBLE);
        if (activity.getCurrentFocus() != null) {
            KeyboardUtil.hideKeyboard(activity.getCurrentFocus());
        }
    }

    /**
     * 与当前面板和待显示的面板不一致，全隐藏，将待显示的面板显示出来
     * @param boundTriggerSubPanelView
     * @param subPanelAndTriggers
     */
    private static void showBoundTriggerSubPanel(final View boundTriggerSubPanelView,
                                                 final SubPanelAndTrigger[] subPanelAndTriggers) {
        // to show bound-trigger panel.
        for (SubPanelAndTrigger panelAndTrigger : subPanelAndTriggers) {
            if (panelAndTrigger.subPanelView != boundTriggerSubPanelView) {
                // other sub panel.
                panelAndTrigger.subPanelView.setVisibility(View.GONE);
            }
        }
        boundTriggerSubPanelView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏语音布局，中间那块按住说话的布局，并同时将按钮样式更改
     * @param subPanelAndTriggers
     */
    private static void hideAudioLayout(SubPanelAndTrigger[] subPanelAndTriggers) {
        for (SubPanelAndTrigger panelAndTrigger : subPanelAndTriggers) {
            if (panelAndTrigger.nType > 0) {
                panelAndTrigger.subPanelView.setVisibility(View.GONE);
                panelAndTrigger.triggerView.setBackgroundResource(R.drawable.nim_chat_add_audio_selector);
            }
        }
    }

    /**
     * 显示键盘
     * @param panelLayout
     * @param focusView
     */
    public static void showKeyboard(final View panelLayout, final View focusView) {
        final Activity activity = (Activity) panelLayout.getContext();

        KeyboardUtil.showKeyboard(focusView);
        if (isHandleByPlaceholder(activity)) {
            panelLayout.setVisibility(View.INVISIBLE);
        }
    }

    static boolean isHandleByPlaceholder(final Activity activity) {
        return isHandleByPlaceholder(AppUtil.isFullScreen(activity),
                AppUtil.isTranslucentStatus(activity), AppUtil.isFitsSystemWindows(activity));
    }
    public static boolean isHandleByPlaceholder(boolean isFullScreen, boolean isTranslucentStatus,
                                                boolean isFitsSystem) {
        return isFullScreen || (isTranslucentStatus && !isFitsSystem);
    }

    /**
     * 同时将输入法和面板都隐藏
     * @param panelLayout
     */
    public static void hidePanelAndKeyboard(final View panelLayout) {
        final Activity activity = (Activity) panelLayout.getContext();

        final View focusView = activity.getCurrentFocus();
        if (focusView != null) {
            KeyboardUtil.hideKeyboard(activity.getCurrentFocus());
            focusView.clearFocus();
        }

        panelLayout.setVisibility(View.GONE);
    }

    public interface SwitchClickListener {
        /**
         * @param switchToPanel true表示转换到了面板，否则相反
         */
        void onClickSwitch(boolean switchToPanel);
    }
    public static class SubPanelAndTrigger {
        /**
         * 子面板
         */
        final View subPanelView;
        /**
         * 触发器即面板切换按钮
         */
        final View triggerView;

        /**
         * 用来判断类型是语音还是表情或者多媒体
         */
        final int nType;

        public SubPanelAndTrigger(View subPanelView, View triggerView, int nType) {
            this.subPanelView = subPanelView;
            this.triggerView = triggerView;
            this.nType = nType;
        }
    }
}
