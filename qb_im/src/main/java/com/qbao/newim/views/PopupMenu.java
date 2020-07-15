package com.qbao.newim.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/20.
 */

public class PopupMenu extends PopupWindow{

    private Activity activity;
    private View popView;

    private View v_item1;
    private View v_item2;
    private View v_item3;
    private View v_item4;

    private OnItemClickListener onItemClickListener;

    public PopupMenu(Activity activity) {
        super(activity);
        this.activity = activity;
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popView = inflater.inflate(R.layout.nim_popup_menu, null);
        this.setContentView(popView);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setTouchable(true);
        this.setOutsideTouchable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);

        v_item1 = popView.findViewById(R.id.menu_group_layout);
        v_item2 = popView.findViewById(R.id.menu_add_friend_layout);
        v_item3 = popView.findViewById(R.id.menu_scan_code_layout);
        v_item4 = popView.findViewById(R.id.menu_setting_layout);

        v_item1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(0);
                }
            }
        });
        v_item2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(1);
                }
            }
        });
        v_item3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(2);
                }
            }
        });
        v_item4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(3);
                }
            }
        });
    }

    /**
     * 设置显示的位置
     *
     */
    public void showLocation(int resource_Id) {
        showAsDropDown(activity.findViewById(resource_Id), dip2px(activity, -112),
                dip2px(activity, -16));
    }

    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public interface OnItemClickListener {
        void onClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}
