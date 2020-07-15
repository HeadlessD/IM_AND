package com.qbao.newim.views.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qbao.newim.adapter.DialogListAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.ScreenUtils;

import java.util.ArrayList;


public class NiftyDialogBuilder extends Dialog implements DialogInterface {

    private final String defTextColor = "#333333";

    private final String defDividerColor = "#11000000";

    private final String defMsgColor = "#333333";

    private final String defDialogColor = "#ffffff";


    private static Context tmpContext;


    private Effectstype type = null;

    private LinearLayout mLinearLayoutView;

    private RelativeLayout mRelativeLayoutView;

    private LinearLayout mLinearLayoutMsgView;

    private LinearLayout mLinearLayoutTopView;

    private FrameLayout mFrameLayoutCustomView;

    private View mDialogView;

    private View mDivider;

    private TextView mTitle;

    private TextView mMessage;

    private ImageView mIcon;

    private Button mButton1;

    private Button mButton2;

    private int mDuration = -1;

    private boolean isCancelable = true;

    private static NiftyDialogBuilder instance;

    public NiftyDialogBuilder(Context context) {
        super(context, R.style.dialog_untran);
        init(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
    }

    public static NiftyDialogBuilder getInstance(Context context) {

        if (instance == null || !tmpContext.equals(context)) {
            synchronized (NiftyDialogBuilder.class) {
                if (instance == null || !tmpContext.equals(context)) {
                    instance = new NiftyDialogBuilder(context);
                }
            }
        }
        tmpContext = context;
        return instance;

    }

    private void init(Context context) {

        mDialogView = View.inflate(context, R.layout.nim_dialog_layout, null);

        mLinearLayoutView = (LinearLayout) mDialogView.findViewById(R.id.parentPanel);
        mRelativeLayoutView = (RelativeLayout) mDialogView.findViewById(R.id.main);
        mLinearLayoutTopView = (LinearLayout) mDialogView.findViewById(R.id.topPanel);
        mLinearLayoutMsgView = (LinearLayout) mDialogView.findViewById(R.id.contentPanel);
        mFrameLayoutCustomView = (FrameLayout) mDialogView.findViewById(R.id.customPanel);

        mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
        mMessage = (TextView) mDialogView.findViewById(R.id.message);
        mIcon = (ImageView) mDialogView.findViewById(R.id.icon);
        mDivider = mDialogView.findViewById(R.id.titleDivider);
        mButton1 = (Button) mDialogView.findViewById(R.id.button1);
        mButton2 = (Button) mDialogView.findViewById(R.id.button2);

        setContentView(mDialogView);

        this.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                mLinearLayoutView.setVisibility(View.VISIBLE);
                if (type == null) {
                    type = Effectstype.Slidetop;
                }
                start(type);


            }
        });
        mRelativeLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCancelable) dismiss();
            }
        });

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mLinearLayoutView.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        mLinearLayoutView.setLayoutParams(params);
    }

    public void toDefault() {
        mTitle.setTextColor(Color.parseColor(defTextColor));
        mDivider.setBackgroundColor(Color.parseColor(defDividerColor));
        mMessage.setTextColor(Color.parseColor(defMsgColor));
        mLinearLayoutView.setBackgroundColor(Color.parseColor(defDialogColor));
    }

    public NiftyDialogBuilder withDividerColor(String colorString) {
        mDivider.setBackgroundColor(Color.parseColor(colorString));
        return this;
    }

    public NiftyDialogBuilder withDividerColor(int color) {
        mDivider.setBackgroundColor(color);
        return this;
    }


    public NiftyDialogBuilder withTitle(CharSequence title) {
        toggleView(mLinearLayoutTopView, title);
        mTitle.setText(title);
        return this;
    }

    public NiftyDialogBuilder withTitleColor(String colorString) {
        mTitle.setTextColor(Color.parseColor(colorString));
        return this;
    }

    public NiftyDialogBuilder withTitleColor(int color) {
        mTitle.setTextColor(color);
        return this;
    }

    public NiftyDialogBuilder withMessage(int textResId) {
        toggleView(mLinearLayoutMsgView, textResId);
        mMessage.setText(textResId);
        return this;
    }

    public NiftyDialogBuilder withMessage(CharSequence msg) {
        if (TextUtils.isEmpty(msg)) {
            mLinearLayoutMsgView.setVisibility(View.GONE);
        } else {
            toggleView(mLinearLayoutMsgView, msg);
            mMessage.setText(msg);
        }
        return this;
    }

    public NiftyDialogBuilder withMessageColor(String colorString) {
        mMessage.setTextColor(Color.parseColor(colorString));
        return this;
    }

    public NiftyDialogBuilder withMessageColor(int color) {
        mMessage.setTextColor(color);
        return this;
    }

    public NiftyDialogBuilder withDialogColor(String colorString) {
        mLinearLayoutView.getBackground().setColorFilter(ColorUtils.getColorFilter(Color.parseColor(colorString)));
        return this;
    }

    public NiftyDialogBuilder withDialogColor(int color) {
        mLinearLayoutView.getBackground().setColorFilter(ColorUtils.getColorFilter(color));
        return this;
    }

    public NiftyDialogBuilder withIcon(int drawableResId) {
        mIcon.setImageResource(drawableResId);
        return this;
    }

    public NiftyDialogBuilder withIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
        return this;
    }

    public NiftyDialogBuilder withDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    public NiftyDialogBuilder withEffect(Effectstype type) {
        this.type = type;
        return this;
    }

    public NiftyDialogBuilder withButtonDrawable(int resid) {
        mButton1.setBackgroundResource(resid);
        mButton2.setBackgroundResource(resid);
        return this;
    }

    public NiftyDialogBuilder withButton1Text(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mButton1.setVisibility(View.GONE);
        } else {
            mButton1.setVisibility(View.VISIBLE);
            mButton1.setText(text);
        }

        return this;
    }

    public NiftyDialogBuilder withButton2Text(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            mButton2.setVisibility(View.GONE);
        } else {
            mButton2.setVisibility(View.VISIBLE);
            mButton2.setText(text);
        }
        return this;
    }

    public NiftyDialogBuilder setButton1Click(final View.OnClickListener click) {
        mButton1.setOnClickListener(click);
        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (click != null) {
                    click.onClick(v);
                }
            }
        });
        return this;
    }

    public NiftyDialogBuilder setButton2Click(final View.OnClickListener click) {
        mButton2.setOnClickListener(click);
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (click != null) {
                    click.onClick(v);
                }
            }
        });
        return this;
    }


    public NiftyDialogBuilder setCustomView(int resId, Context context) {
        View customView = View.inflate(context, resId, null);
        mMessage.setVisibility(View.GONE);
        mTitle.setVisibility(View.GONE);
        mButton1.setVisibility(View.GONE);
        mButton2.setVisibility(View.GONE);
        mLinearLayoutTopView.setVisibility(View.GONE);
        if (mFrameLayoutCustomView.getChildCount() > 0) {
            mFrameLayoutCustomView.removeAllViews();
        }
        mFrameLayoutCustomView.addView(customView);
        return this;
    }

//    public NiftyDialogBuilder setEditView(Context context) {
//        mMessage.setVisibility(View.VISIBLE);
//        mTitle.setVisibility(View.GONE);
//        mLinearLayoutTopView.setVisibility(View.GONE);
//
//        int width = ScreenUtils.getSceenWidth(context);
//        EditText editText = new EditText(context);
//
//        mRelativeLayoutView.setPadding(width / 6, 0, width / 6, 0);
//
//        if (mFrameLayoutCustomView.getChildCount() > 0) {
//            mFrameLayoutCustomView.removeAllViews();
//        }
//        mFrameLayoutCustomView.addView(editText);
//
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)editText.getLayoutParams();
//        params.width = width / 2;
//        params.gravity = Gravity.CENTER;
//        params.setMargins(0, 0, 0, 20);
//        editText.setLayoutParams(params);
//
//        return this;
//    }

    public NiftyDialogBuilder setCustomView(View view) {
        mMessage.setVisibility(view == null ? View.VISIBLE : View.GONE);
        mTitle.setVisibility(view == null ? View.VISIBLE : View.GONE);
        mLinearLayoutTopView.setVisibility(view == null ?View.VISIBLE : View.GONE);

        if (view != null) {
            if (mFrameLayoutCustomView.getChildCount() > 0) {
                mFrameLayoutCustomView.removeAllViews();
            }
            mFrameLayoutCustomView.addView(view);
            mRelativeLayoutView.setPadding(0, 0, 0, 10);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mLinearLayoutView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mLinearLayoutView.setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mLinearLayoutView.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            mLinearLayoutView.setLayoutParams(params);
        }

        return this;
    }

    public NiftyDialogBuilder setCustomViewDialog(View view) {
        mMessage.setVisibility(View.GONE);
        mTitle.setVisibility(View.VISIBLE);
        mLinearLayoutTopView.setVisibility(View.VISIBLE);

        if (view != null) {
            if (mFrameLayoutCustomView.getChildCount() > 0) {
                mFrameLayoutCustomView.removeAllViews();
            }

            mFrameLayoutCustomView.addView(view);
            mFrameLayoutCustomView.setPadding(0, 80, 0, 80);
            int width = ScreenUtils.getSceenWidth(view.getContext());
            mRelativeLayoutView.setPadding(width / 10, 0, width / 10, 0);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mLinearLayoutView.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            mLinearLayoutView.setLayoutParams(params);
        }

        return this;
    }

    public NiftyDialogBuilder setCustomListView(Context context, ArrayList<String> data,
                                                final BaseQuickAdapter.OnItemClickListener listener) {
        int width = ScreenUtils.getSceenWidth(context);

        if (data == null || data.isEmpty()) {
            if (mFrameLayoutCustomView.getChildCount() > 0) {
                mFrameLayoutCustomView.removeAllViews();
            }
            mRelativeLayoutView.setPadding(width / 10, 0, width / 10, 0);
            return this;
        }

        View customView = View.inflate(context, R.layout.nim_dialog_list, null);

        if (mFrameLayoutCustomView.getChildCount() > 0) {
            mFrameLayoutCustomView.removeAllViews();
        }

        mLinearLayoutMsgView.setVisibility(View.GONE);
        mRelativeLayoutView.setPadding(width / 6, 0, width / 6, 0);

        RecyclerView recyclerView = (RecyclerView) customView.findViewById(R.id.dialog_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        DialogListAdapter adapter = new DialogListAdapter(data);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                dismiss();
                if (listener != null) {
                    listener.onItemClick(adapter, view, position);
                }
            }
        });

        mFrameLayoutCustomView.addView(customView);
        return this;
    }

    public NiftyDialogBuilder isCancelableOnTouchOutside(boolean cancelable) {
        this.isCancelable = cancelable;
        this.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    public NiftyDialogBuilder isCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
        this.setCancelable(cancelable);
        return this;
    }

    private void toggleView(View view, Object obj) {
        if (obj == null) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        super.show();
    }

    private void start(Effectstype type) {
        BaseEffects animator = type.getAnimator();
        if (mDuration != -1) {
            animator.setDuration(Math.abs(mDuration));
        }
        animator.start(mRelativeLayoutView);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mButton1.setVisibility(View.GONE);
        mButton2.setVisibility(View.GONE);
    }
}
