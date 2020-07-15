package com.qbao.newim.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.qbao.newim.activity.ChooseSessionActivity;
import com.qbao.newim.activity.GroupCreateActivity;
import com.qbao.newim.activity.GroupInviteDetailActivity;
import com.qbao.newim.activity.GroupManagerActivity;
import com.qbao.newim.activity.GroupMemberActivity;
import com.qbao.newim.activity.GroupRemarkActivity;
import com.qbao.newim.activity.GroupSelectActivity;
import com.qbao.newim.activity.NIMBaiduMapActivity;
import com.qbao.newim.activity.NIMChooseCardActivity;
import com.qbao.newim.activity.NIMEditNameActivity;
import com.qbao.newim.activity.NIMEncodeActivity;
import com.qbao.newim.activity.NIMGcChatActivity;
import com.qbao.newim.activity.NIMReportActivity;
import com.qbao.newim.activity.NIMScChatActivity;
import com.qbao.newim.activity.NIMScanGroupActivity;
import com.qbao.newim.activity.NIMUserInfoActivity;
import com.qbao.newim.niminterface.INIMViewDelegate;

/**
 * Created by chenjian on 2017/6/27.
 */

public class NIMStartActivityUtil {
    private static INIMViewDelegate m_view_delegate = null;

    public static void setViewDelegate(INIMViewDelegate view_delegate) {
        m_view_delegate = view_delegate;
    }
    /**
     * 跳转到二维码名片界面
     * @param context 当前需要跳转的界面
     * @param id 当前用户id，或者group_id
     * @param is_user 当前是个人用户，还是群聊二维码
     */
    public static void startToNIMEncodeActivity(Context context, long id, boolean is_user) {
//        NIMEncodeActivity.startUserEncode(context, id, is_user);
        Intent intent = new Intent(context, NIMEncodeActivity.class);
        intent.putExtra("is_user", is_user);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }

    /**
     * 跳转到用户详情界面界面
     * @param context 当前需要跳转的界面
     * @param user_id 当前用户id，或者group_id
     * @param source_type 该用户从哪个界面点击进来
     */
    public static void startToNIMUserActivity(Context context, long user_id, byte source_type) {
        Intent intent = new Intent(context, NIMUserInfoActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("source_type", source_type);
        context.startActivity(intent);
    }

    public static void startToUserForResult(Activity context, long user_id, byte source_type, int code, int pos) {
        Intent intent = new Intent(context, NIMUserInfoActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("source_type", source_type);
        intent.putExtra("pos", pos);
        context.startActivityForResult(intent, code);
    }

    /**
     * 跳转到转让群主界面或者群成员界面
     * @param context 当前需要跳转的界面
     * @param group_id group_id
     * @param admin 是否群主
     */
    public static void startToNIMGroupMemberActivity(Context context, long group_id, boolean admin, int type) {
        Intent intent = new Intent(context, GroupMemberActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("admin", admin);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }
    public static void startToNGMAForResult(Activity context, long group_id, int type, int code) {
        Intent intent = new Intent(context, GroupMemberActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("type", type);
        context.startActivityForResult(intent, code);
    }

    /**
     * 跳转到群邀请界面或者新增群成员界面
     * @param context 当前需要跳转的界面
     * @param setting_id group_id 或者当前用户user_id
     */
    public static void startToNIMGroupCreateActivity(Context context, long setting_id, int model) {
        Intent intent = new Intent(context, GroupCreateActivity.class);
        intent.putExtra("setting_id", setting_id);
        intent.putExtra("model", model);
        context.startActivity(intent);
    }
    public static void startToNGCAForResult(Activity context, long setting_id, int model, int result_code) {
        Intent intent = new Intent(context, GroupCreateActivity.class);
        intent.putExtra("setting_id", setting_id);
        intent.putExtra("model", model);
        context.startActivityForResult(intent, result_code);
    }

    /**
     * 跳转到名字编辑界面
     * @param context 当前需要跳转的界面
     * @param setting_id group_id 或者当前用户user_id
     * @param type 当前需要编辑的类型
     * @param content 当前编辑前内容
     */
    public static void startToNIMEditActivity(Context context, long setting_id, int type, String content) {
        Intent intent = new Intent(context, NIMEditNameActivity.class);
        intent.putExtra("setting_id", setting_id);
        intent.putExtra("type", type);
        intent.putExtra("content", content);
        context.startActivity(intent);
    }

    /**
     * 跳转到群管理界面
     * @param context 当前需要跳转的界面
     * @param group_id group_id
     */
    public static void startToGroupManagerActivity(Context context, long group_id) {
        Intent intent = new Intent(context, GroupManagerActivity.class);
        intent.putExtra("group_id", group_id);
        context.startActivity(intent);
    }

    /**
     * 跳转到群公告界面
     * @param context 当前需要跳转的界面
     * @param group_id group_id
     */
    public static void startToGroupRemarkActivity(Context context, long group_id) {
        Intent intent = new Intent(context, GroupManagerActivity.class);
        intent.putExtra("group_id", group_id);
        context.startActivity(intent);
    }

    /**
     * 跳转到聊天界面
     * @param context 当前需要跳转的界面
     * @param send_id 当前group_id或者user_id
     */
    public static void startToScActivity(Activity context, long send_id) {
        AppUtil.exit();
        Intent intent = new Intent(context, NIMScChatActivity.class);
        intent.putExtra("id", send_id);
        context.startActivity(intent);
        context.onBackPressed();
    }

    public static void startToGcActivity(Activity context, long send_id) {
        AppUtil.exit();
        Intent intent = new Intent(context, NIMGcChatActivity.class);
        intent.putExtra("id", send_id);
        context.startActivity(intent);
        context.onBackPressed();
    }

    /**
     * 跳转到群邀请详情界面
     * @param context 当前需要跳转的界面
     * @param group_id 当前group_id
     */
    public static void startToInviteDetailActivity(Context context, long user_id, long group_id, long message_id) {
        Intent intent = new Intent(context, GroupInviteDetailActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("group_id", group_id);
        intent.putExtra("message_id", message_id);
        context.startActivity(intent);
    }

    /**
     * 跳转到群公告详情界面
     * @param context 当前需要跳转的界面
     * @param group_id 当前group_id
     */
    public static void startToNIMRemarkActivity(Context context, long group_id, boolean manager, String remark) {
        Intent intent = new Intent(context, GroupRemarkActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("manager", manager);
        intent.putExtra("remark", remark);
        context.startActivity(intent);
    }

    public static void startToScanGroupActivity(Activity context, long group_id, long user_id) {
        Intent intent = new Intent(context, NIMScanGroupActivity.class);
        intent.putExtra("group_id", group_id);
        intent.putExtra("user_id", user_id);
        context.startActivity(intent);
        context.finish();
    }

    /**
     * 发送实时位置给好友
     * @param context
     * @param code
     */
    public static void startToLocationActivityForResult(Activity context, int code) {
        Intent intent = new Intent(context, NIMBaiduMapActivity.class);
        context.startActivityForResult(intent, code);
    }

    /**
     * 查看好友发送的位置
     * @param context
     * @param lat
     * @param lon
     * @param address
     */
    public static void startToLocationActivity(Context context, double lat, double lon, String address) {
        Intent intent = new Intent(context, NIMBaiduMapActivity.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lon", lon);
        intent.putExtra("address", address);
        context.startActivity(intent);
    }

    /**
     * 发送名片
     * @param context
     * @param code
     */
    public static void startToCardActivity(Activity context, String data, int code) {
        Intent intent = new Intent(context, NIMChooseCardActivity.class);
        intent.putExtra("data", data);
        context.startActivityForResult(intent, code);
    }

    /**
     * 转发消息选择联系人
     * @param context
     */
    public static void startToChooseSessionActivity(Context context, String data) {
        Intent intent = new Intent(context, ChooseSessionActivity.class);
        intent.putExtra("data", data);
        context.startActivity(intent);
    }

    /**
     * 选择群聊
     * @param context
     */
    public static void startToChooseGroupActivity(Activity context, String data, int code) {
        Intent intent = new Intent(context, GroupSelectActivity.class);
        intent.putExtra("data", data);
        context.startActivityForResult(intent, code);
    }

    public static void startToReportActivity(Context context, long user_id) {
        Intent intent = new Intent(context, NIMReportActivity.class);
        intent.putExtra("user_id", user_id);
        context.startActivity(intent);
    }

    public static void startToTask(Activity activity) {
        if(m_view_delegate != null)
        {
            m_view_delegate.OnEnterTask(activity);
        }
    }

    public static void startToSubscribe(Activity activity) {
        if(m_view_delegate != null)
        {
            m_view_delegate.OnEnterSubscribe(activity);
        }
    }

    public static void startToOfficialContact(Activity activity) {
        if(m_view_delegate != null)
        {
            m_view_delegate.OnEnterOfficialContact(activity);
        }
    }

    public static void startToOfficialChat(Activity activity, long official_id) {
        if(m_view_delegate != null)
        {
            m_view_delegate.OnEnterOffcialChat(activity, official_id);
        }
    }
}
