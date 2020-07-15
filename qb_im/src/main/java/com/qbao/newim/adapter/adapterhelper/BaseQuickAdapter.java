package com.qbao.newim.adapter.adapterhelper;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.qbao.newim.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.ListPopupWindow.WRAP_CONTENT;

/**
 * Created by chenjian on 2017/3/22.
 * 基类Adapter
 */

public abstract class BaseQuickAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {

    //提供加载更多功能
    private boolean mNextLoadEnable = false;
    private boolean mLoadMoreEnable = false;
    private boolean mLoading = false;
    private LoadMoreView mLoadMoreView = new SimpleLoadMoreView();
    private RequestLoadMoreListener mRequestLoadMoreListener;

    // 使用动画类型 这里是每条item入场动画和出场动画
    public static final int ALPHAIN = 0x00000001;
    public static final int SCALEIN = 0x00000002;
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    public static final int SLIDEIN_LEFT = 0x00000004;
    public static final int SLIDEIN_RIGHT = 0x00000005;

    // item和item中child点击和长按事件
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemChildClickListener mOnItemChildClickListener;
    private OnItemChildLongClickListener mOnItemChildLongClickListener;

    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    // 表示动画类型是否只是第一次绘制展示
    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    private int mLastPosition = -1;

    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();
    //头部和底部
    private LinearLayout mHeaderLayout;
    private LinearLayout mFooterLayout;
    //空数据状态
    private FrameLayout mEmptyLayout;
    private boolean mIsUseEmpty = true;
    private boolean mHeadAndEmptyEnable;
    private boolean mFootAndEmptyEnable;

    protected static final String TAG = BaseQuickAdapter.class.getSimpleName();
    protected Context mContext;
    protected int mLayoutResId;
    protected LayoutInflater mLayoutInflater;
    protected List<T> mData;
    public static final int HEADER_VIEW = 0x00000111;
    public static final int LOADING_VIEW = 0x00000222;
    public static final int FOOTER_VIEW = 0x00000333;
    public static final int EMPTY_VIEW = 0x00000555;

    private RecyclerView mRecyclerView;

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    private void checkNotNull() {
        if (getRecyclerView() == null) {
            throw new RuntimeException("please bind recyclerView first!");
        }
    }

    /**
     * 类似于Recycleview的setAdapter，并保存recyclerView的实例
     */
    public void bindToRecyclerView(RecyclerView recyclerView) {
        if (getRecyclerView() != null) {
            throw new RuntimeException("Don't bind twice");
        }
        setRecyclerView(recyclerView);
        getRecyclerView().setAdapter(this);
    }

    private void openLoadMore(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
        mNextLoadEnable = true;
        mLoadMoreEnable = true;
        mLoading = false;
    }

    public void setOnLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener, RecyclerView recyclerView) {
        openLoadMore(requestLoadMoreListener);
        if (getRecyclerView() == null) {
            setRecyclerView(recyclerView);
        }
    }

    /**
     * 使用前先绑定对应的RecyclerView，即RecyclerView不能为空
     */
    public void disableLoadMoreIfNotFullPage() {
        checkNotNull();
        disableLoadMoreIfNotFullPage(getRecyclerView());
    }

    /**
     * 确定是否有更多数据，来展示加载更多布局
     */
    public void disableLoadMoreIfNotFullPage(RecyclerView recyclerView) {
        if (recyclerView == null) return;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager == null) return;
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((linearLayoutManager.findLastCompletelyVisibleItemPosition() + 1) != getItemCount()) {
                        setEnableLoadMore(true);
                    }
                }
            }, 50);
        } else if (manager instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) manager;
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int[] positions = new int[staggeredGridLayoutManager.getSpanCount()];
                    staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(positions);
                    int pos = getTheBiggestNumber(positions) + 1;
                    if (pos != getItemCount()) {
                        setEnableLoadMore(true);
                    }
                }
            }, 50);
        }
    }

    private int getTheBiggestNumber(int[] numbers) {
        int tmp = -1;
        if (numbers == null || numbers.length == 0) {
            return tmp;
        }
        for (int num : numbers) {
            if (num > tmp) {
                tmp = num;
            }
        }
        return tmp;
    }

    public void setNotDoAnimationCount(int count) {
        mLastPosition = count;
    }

    /**
     * 可自定义加载更多布局
     *
     * @param loadingView
     */
    public void setLoadMoreView(LoadMoreView loadingView) {
        this.mLoadMoreView = loadingView;
    }

    /**
     * 当前加载更多布局个数
     * @return 0 or 1
     */
    public int getLoadMoreViewCount() {
        if (mRequestLoadMoreListener == null || !mLoadMoreEnable) {
            return 0;
        }
        if (!mNextLoadEnable && mLoadMoreView.isLoadEndMoreGone()) {
            return 0;
        }
        if (mData.size() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * @return 当前adapter是否正在加载数据
     * progress.
     */
    public boolean isLoading() {
        return mLoading;
    }


    /**
     * 加载完成后，没有更多数据了
     */
    public void loadMoreEnd() {
        loadMoreEnd(false);
    }

    /**
     * 加载完成后，没有更多数据了
     *
     * @param
     */
    public void loadMoreEnd(boolean gone) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mNextLoadEnable = false;
        mLoadMoreView.setLoadMoreEndGone(gone);
        if (gone) {
            notifyItemRemoved(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
        } else {
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_END);
            notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
        }
    }

    /**
     * 加载完成
     */
    public void loadMoreComplete() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
    }

    /**
     * 加载失败
     */
    public void loadMoreFail() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);
        notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
    }

    /**
     * 设置加载的状态
     *
     * @param enable
     */
    public void setEnableLoadMore(boolean enable) {
        int oldLoadMoreCount = getLoadMoreViewCount();
        mLoadMoreEnable = enable;
        int newLoadMoreCount = getLoadMoreViewCount();

        if (oldLoadMoreCount == 1) {
            if (newLoadMoreCount == 0) {
                notifyItemRemoved(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
            }
        } else {
            if (newLoadMoreCount == 1) {
                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                notifyItemInserted(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
            }
        }
    }

    /**
     * 返回当前加载状态
     *
     * @return
     */
    public boolean isLoadMoreEnable() {
        return mLoadMoreEnable;
    }

    /**
     * 设置加载动画时间
     *
     * @param
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }


    /**
     * 构造函数
     * @param layoutResId item布局
     * @param data        要显示的数据集合
     */
    public BaseQuickAdapter(int layoutResId, List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    public BaseQuickAdapter(List<T> data) {
        this(0, data);
    }

    public BaseQuickAdapter(int layoutResId) {
        this(layoutResId, null);
    }

    /**
     * 设置新的数据，即刷新新数据
     *
     * @param data
     */
    public void setNewData(List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true;
            mLoadMoreEnable = true;
            mLoading = false;
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }
        mLastPosition = -1;
        notifyDataSetChanged();
    }


    /**
     * 添加一个新的数据到指定位置
     *
     * @param position
     */
    public void addData(int position, T data) {
        mData.add(position, data);
        notifyItemInserted(position + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * 添加一个数据到最后面
     */
    public void addData(T data) {
        mData.add(data);
        notifyItemInserted(mData.size() + getHeaderLayoutCount());
        compatibilityDataSizeChanged(1);
    }

    /**
     * 移除指定位置的数据
     *
     * @param position
     */
    public void remove(int position) {
        mData.remove(position);
        notifyItemRemoved(position + getHeaderLayoutCount());
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(position, mData.size() - position);
    }

    /**
     * 修改指定位置的数据
     */
    public void setData(int index, T data) {
        mData.set(index, data);
        notifyItemChanged(index + getHeaderLayoutCount());
    }
    public void moveData(int from_index, int to_index, T data) {
        Utils.swapSession(mData, from_index, to_index);
        notifyItemMoved(from_index, to_index);
        mData.set(to_index, data);
        notifyItemChanged(to_index);
    }

    public void setTopData(int index, T data) {
        if (index == 0) {
            setData(0, data);
            return;
        }
        mData.remove(index);
        mData.add(0, data);
        notifyItemMoved(index, 0);
        notifyItemChanged(0);
    }

    public void setTopData(int top_index, int index, T data) {
        if (index == 0) {
            setData(0, data);
            return;
        }
        mData.remove(index);
        mData.add(top_index, data);
        notifyItemMoved(index, top_index);
        notifyItemChanged(top_index);

    }

    /**
     * 新加一个数据集合到指定位置
     *
     * @param position
     */
    public void addData(int position, List<T> data) {
        mData.addAll(position, data);
        notifyItemRangeInserted(position + getHeaderLayoutCount(), data.size());
        compatibilityDataSizeChanged(data.size());
    }

    /**
     * 添加数据集合到原数据后面
     *
     * @param newData
     */
    public void addData(List<T> newData) {
        this.mData.addAll(newData);
        notifyItemRangeInserted(mData.size() - newData.size() + getHeaderLayoutCount(), newData.size());
        compatibilityDataSizeChanged(newData.size());
    }

    /**
     * 更新数据
     *
     * @param size
     */
    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mData == null ? 0 : mData.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    /**
     * 获取当前数据集合
     *
     * @return
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * 获取指定位置的数据
     */
    public T getItem(int position) {
        if (position != -1)
            return mData.get(position);
        else
            return null;
    }

    /**
     * 判断当前头布局是否存在
     */
    public int getHeaderLayoutCount() {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * 判断底布局是否存在
     */
    public int getFooterLayoutCount() {
        if (mFooterLayout == null || mFooterLayout.getChildCount() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * 当前是否存在可显示view
     *
     * @return
     */
    public int getEmptyViewCount() {
        if (mEmptyLayout == null || mEmptyLayout.getChildCount() == 0) {
            return 0;
        }
        if (!mIsUseEmpty) {
            return 0;
        }
        if (mData.size() != 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        int count;
        if (getEmptyViewCount() == 1) {
            count = 1;
            if (mHeadAndEmptyEnable && getHeaderLayoutCount() != 0) {
                count++;
            }
            if (mFootAndEmptyEnable && getFooterLayoutCount() != 0) {
                count++;
            }
        } else {
            count = getHeaderLayoutCount() + mData.size() + getFooterLayoutCount() + getLoadMoreViewCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            boolean header = mHeadAndEmptyEnable && getHeaderLayoutCount() != 0;
            switch (position) {
                case 0:
                    if (header) {
                        return HEADER_VIEW;
                    } else {
                        return EMPTY_VIEW;
                    }
                case 1:
                    if (header) {
                        return EMPTY_VIEW;
                    } else {
                        return FOOTER_VIEW;
                    }
                case 2:
                    return FOOTER_VIEW;
                default:
                    return EMPTY_VIEW;
            }
        }
        autoLoadMore(position);
        int numHeaders = getHeaderLayoutCount();
        if (position < numHeaders) {
            return HEADER_VIEW;
        } else {
            int adjPosition = position - numHeaders;
            int adapterCount = mData.size();
            if (adjPosition < adapterCount) {
                return getDefItemViewType(adjPosition);
            } else {
                adjPosition = adjPosition - adapterCount;
                int numFooters = getFooterLayoutCount();
                if (adjPosition < numFooters) {
                    return FOOTER_VIEW;
                } else {
                    return LOADING_VIEW;
                }
            }
        }
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        K baseViewHolder = null;
        this.mContext = parent.getContext();
        this.mLayoutInflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                break;
            case HEADER_VIEW:
                baseViewHolder = createBaseViewHolder(mHeaderLayout);
                break;
            case EMPTY_VIEW:
                baseViewHolder = createBaseViewHolder(mEmptyLayout);
                break;
            case FOOTER_VIEW:
                baseViewHolder = createBaseViewHolder(mFooterLayout);
                break;
            default:
                baseViewHolder = onCreateDefViewHolder(parent, viewType);
                bindViewClickListener(baseViewHolder);
        }
        baseViewHolder.setAdapter(this);
        return baseViewHolder;

    }


    private K getLoadingView(ViewGroup parent) {
        View view = getItemView(mLoadMoreView.getLayoutId(), parent);
        K holder = createBaseViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                    mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                    notifyItemChanged(getHeaderLayoutCount() + mData.size() + getFooterLayoutCount());
                }
            }
        });
        return holder;
    }

    /**
     * 每显示一个View，绑定到Window
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    /**
     * 占领全部空间 即比例占1
     * @param holder
     */
    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (mSpanSizeLookup == null)
                        return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type ==
                                LOADING_VIEW) ? gridManager.getSpanCount() : 1;
                    else
                        return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type ==
                                LOADING_VIEW) ? gridManager.getSpanCount() : mSpanSizeLookup.getSpanSize(gridManager,
                                position - getHeaderLayoutCount());
                }
            });
        }
    }

    private SpanSizeLookup mSpanSizeLookup;

    public interface SpanSizeLookup {
        int getSpanSize(GridLayoutManager gridLayoutManager, int position);
    }

    /**
     *  设置每个item共多少个view存在
     */
    public void setSpanSizeLookup(SpanSizeLookup spanSizeLookup) {
        this.mSpanSizeLookup = spanSizeLookup;
    }

    /**
     * 不同类型的holder绑定不同的事件
     *
     * @param holder
     * @param positions
     */
    @Override
    public void onBindViewHolder(K holder, int positions) {
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case 0:

                convert(holder, mData.get(holder.getLayoutPosition() - getHeaderLayoutCount()));
                break;
            case LOADING_VIEW:
                mLoadMoreView.convert(holder);
                break;
            case HEADER_VIEW:
                break;
            case EMPTY_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            default:
                convert(holder, mData.get(holder.getLayoutPosition() - getHeaderLayoutCount()));
                break;
        }
    }

    private void bindViewClickListener(final BaseViewHolder baseViewHolder) {
        if (baseViewHolder == null) {
            return;
        }
        final View view = baseViewHolder.getConvertView();
        if (view == null) {
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOnItemClickListener() != null&&baseViewHolder!=null) {

                    getOnItemClickListener().onItemClick(BaseQuickAdapter.this, v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }

            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (getOnItemLongClickListener() != null&&baseViewHolder!=null) {
                    return getOnItemLongClickListener().onItemLongClick(BaseQuickAdapter.this, v, baseViewHolder.getLayoutPosition() - getHeaderLayoutCount());
                }
                return false;

            }
        });
    }

    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, mLayoutResId);
    }

    protected K createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return createBaseViewHolder(getItemView(layoutResId, parent));
    }

    /**
     * 重新创建一个BaseViewHolder
     * @param view view
     * @return new ViewHolder
     */
    protected K createBaseViewHolder(View view) {
        Class temp = getClass();
        Class z = null;
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp);
            temp = temp.getSuperclass();
        }
        K k = createGenericKInstance(z, view);
        return null != k ? k : (K) new BaseViewHolder(view);
    }

    /**
     * 创建viewholder的实例
     *
     * @param z
     * @param view
     * @return
     */
    private K createGenericKInstance(Class z, View view) {
        try {
            Constructor constructor;
            String buffer = Modifier.toString(z.getModifiers());
            String className = z.getName();
            // inner and unstatic class
            if (className.contains("$") && !buffer.contains("static")) {
                constructor = z.getDeclaredConstructor(getClass(), View.class);
                return (K) constructor.newInstance(this, view);
            } else {
                constructor = z.getDeclaredConstructor(View.class);
                return (K) constructor.newInstance(view);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前class的基本类型，并判断当前class是否属于BaseViewHolder或者是其子类
     *Type是 Java 编程语言中所有类型的公共高级接口。它们包括原始类型、参数化类型、数组类型、类型变量和基本类型。
     * @param z
     * @return
     */
    private Class getInstancedGenericKClass(Class z) {
        Type type = z.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type temp : types) {
                if (temp instanceof Class) {
                    Class tempClass = (Class) temp;
                    if (BaseViewHolder.class.isAssignableFrom(tempClass)) {
                        return tempClass;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 得到当前头布局
     */

    public LinearLayout getHeaderLayout() {
        return mHeaderLayout;
    }

    /**
     * 当前底布局
     */
    public LinearLayout getFooterLayout() {
        return mFooterLayout;
    }

    /**
     * 添加头布局到第一个位置
     *
     * @param header
     */
    public int addHeaderView(View header) {
        return addHeaderView(header, -1);
    }

    /**
     * 如果添加了多个头布局，再添加头布局到指定位置
     */
    public int addHeaderView(View header, int index) {
        return addHeaderView(header, index, LinearLayout.VERTICAL);
    }

    /**
     * 添加到头布局，涉及到不同方向
     * @param header
     * @param index
     * @param orientation
     */
    public int addHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null) {
            mHeaderLayout = new LinearLayout(header.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mHeaderLayout.setOrientation(LinearLayout.VERTICAL);
                mHeaderLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
                mHeaderLayout.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mHeaderLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mHeaderLayout.addView(header, index);
        if (mHeaderLayout.getChildCount() == 1) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setHeaderView(View header) {
        return setHeaderView(header, 0, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index) {
        return setHeaderView(header, index, LinearLayout.VERTICAL);
    }

    public int setHeaderView(View header, int index, int orientation) {
        if (mHeaderLayout == null || mHeaderLayout.getChildCount() <= index) {
            return addHeaderView(header, index, orientation);
        } else {
            mHeaderLayout.removeViewAt(index);
            mHeaderLayout.addView(header, index);
            return index;
        }
    }

    /**
     * 添加底布局
     *
     * @param footer
     */
    public int addFooterView(View footer) {
        return addFooterView(footer, -1, LinearLayout.VERTICAL);
    }

    public int addFooterView(View footer, int index) {
        return addFooterView(footer, index, LinearLayout.VERTICAL);
    }

    /**
     * 与添加头布局类似
     */
    public int addFooterView(View footer, int index, int orientation) {
        if (mFooterLayout == null) {
            mFooterLayout = new LinearLayout(footer.getContext());
            if (orientation == LinearLayout.VERTICAL) {
                mFooterLayout.setOrientation(LinearLayout.VERTICAL);
                mFooterLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            } else {
                mFooterLayout.setOrientation(LinearLayout.HORIZONTAL);
                mFooterLayout.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
            }
        }
        final int childCount = mFooterLayout.getChildCount();
        if (index < 0 || index > childCount) {
            index = childCount;
        }
        mFooterLayout.addView(footer, index);
        if (mFooterLayout.getChildCount() == 1) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemInserted(position);
            }
        }
        return index;
    }

    public int setFooterView(View header) {
        return setFooterView(header, 0, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index) {
        return setFooterView(header, index, LinearLayout.VERTICAL);
    }

    public int setFooterView(View header, int index, int orientation) {
        if (mFooterLayout == null || mFooterLayout.getChildCount() <= index) {
            return addFooterView(header, index, orientation);
        } else {
            mFooterLayout.removeViewAt(index);
            mFooterLayout.addView(header, index);
            return index;
        }
    }

    /**
     * 移除头布局，如果是最后一个，则把头布局设置为空
     *
     * @param header
     */
    public void removeHeaderView(View header) {
        if (getHeaderLayoutCount() == 0) return;

        mHeaderLayout.removeView(header);
        if (mHeaderLayout.getChildCount() == 0) {
            int position = getHeaderViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * 移除底布局，如果是最后一个，则把底布局设置为空
     *
     * @param footer
     */
    public void removeFooterView(View footer) {
        if (getFooterLayoutCount() == 0) return;

        mFooterLayout.removeView(footer);
        if (mFooterLayout.getChildCount() == 0) {
            int position = getFooterViewPosition();
            if (position != -1) {
                notifyItemRemoved(position);
            }
        }
    }

    /**
     * 移除所有头布局
     */
    public void removeAllHeaderView() {
        if (getHeaderLayoutCount() == 0) return;

        mHeaderLayout.removeAllViews();
        int position = getHeaderViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    /**
     * 移除所有底布局
     */
    public void removeAllFooterView() {
        if (getFooterLayoutCount() == 0) return;

        mFooterLayout.removeAllViews();
        int position = getFooterViewPosition();
        if (position != -1) {
            notifyItemRemoved(position);
        }
    }

    private int getHeaderViewPosition() {
        //返回当前头布局在RecyclerView位置，没有的话就返回-1
        if (getEmptyViewCount() == 1) {
            if (mHeadAndEmptyEnable) {
                return 0;
            }
        } else {
            return 0;
        }
        return -1;
    }

    private int getFooterViewPosition() {
        //返回当前底布局在RecyclerView位置，没有的话就返回-1
        if (getEmptyViewCount() == 1) {
            int position = 1;
            if (mHeadAndEmptyEnable && getHeaderLayoutCount() != 0) {
                position++;
            }
            if (mFootAndEmptyEnable) {
                return position;
            }
        } else {
            return getHeaderLayoutCount() + mData.size();
        }
        return -1;
    }

    public void setEmptyView(int layoutResId, ViewGroup viewGroup) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        setEmptyView(view);
    }

    /**
     * 设置空布局，设置前检查RecyclerView是否存在
     */
    public void setEmptyView(int layoutResId) {
        checkNotNull();
        setEmptyView(layoutResId, getRecyclerView());
    }

    public void setEmptyView(View emptyView) {
        boolean insert = false;
        if (mEmptyLayout == null) {
            mEmptyLayout = new FrameLayout(emptyView.getContext());
            final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final ViewGroup.LayoutParams lp = emptyView.getLayoutParams();
            if (lp != null) {
                layoutParams.width = lp.width;
                layoutParams.height = lp.height;
            }
            mEmptyLayout.setLayoutParams(layoutParams);
            insert = true;
        }
        mEmptyLayout.removeAllViews();
        mEmptyLayout.addView(emptyView);
        mIsUseEmpty = true;
        if (insert) {
            if (getEmptyViewCount() == 1) {
                int position = 0;
                if (mHeadAndEmptyEnable && getHeaderLayoutCount() != 0) {
                    position++;
                }
                notifyItemInserted(position);
            }
        }
    }

    /**
     * 有头布局存在，但是item是空的
     */
    public void setHeaderAndEmpty(boolean isHeadAndEmpty) {
        setHeaderFooterEmpty(isHeadAndEmpty, false);
    }

    /**
     * 有头布局和底布局存在，但是item是空的
     *
     * @param isHeadAndEmpty
     * @param isFootAndEmpty
     */
    public void setHeaderFooterEmpty(boolean isHeadAndEmpty, boolean isFootAndEmpty) {
        mHeadAndEmptyEnable = isHeadAndEmpty;
        mFootAndEmptyEnable = isFootAndEmpty;
    }

    /**
     * 是否使用空布局
     *
     * @param isUseEmpty
     */
    public void isUseEmpty(boolean isUseEmpty) {
        mIsUseEmpty = isUseEmpty;
    }

    /**
     * 空布局时候的样式
     *
     * @return
     */
    public View getEmptyView() {
        return mEmptyLayout;
    }

    private int mAutoLoadMoreSize = 1;

    public void setAutoLoadMoreSize(int autoLoadMoreSize) {
        if (autoLoadMoreSize > 1) {
            mAutoLoadMoreSize = autoLoadMoreSize;
        }
    }

    private void autoLoadMore(int position) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        if (position < getItemCount() - mAutoLoadMoreSize) {
            return;
        }
        if (mLoadMoreView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {
            return;
        }
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
        if (!mLoading) {
            mLoading = true;
            if (getRecyclerView() != null) {
                getRecyclerView().post(new Runnable() {
                    @Override
                    public void run() {
                        mRequestLoadMoreListener.onLoadMoreRequested();
                    }
                });
            } else {
                mRequestLoadMoreListener.onLoadMoreRequested();
            }
        }
    }


    /**
     * 添加动画
     *
     * @param holder
     */
    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation = null;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.getLayoutPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * 每个item设置动画
     *
     * @param anim
     * @param index
     */
    protected void startAnim(Animator anim, int index) {
        anim.setDuration(mDuration).start();
        anim.setInterpolator(mInterpolator);
    }

    /**
     * @param layoutResId ID for an XML layout resource to load
     * @param parent      Optional view to be the parent of the generated hierarchy or else simply an object that
     *                    provides a set of LayoutParams values for root of the returned
     *                    hierarchy
     * @return view will be return
     */
    protected View getItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }


    public interface RequestLoadMoreListener {

        void onLoadMoreRequested();

    }


    /**
     * 设置动画显示类型
     *
     * @param animationType ALPHAIN SCALEIN SLIDEIN_BOTTOM SLIDEIN_LEFT SLIDEIN_RIGHT
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * 也可以自定义动画类型
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    /**
     * 加载item 的时候开启动画
     */
    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    /**
     * 是否只在第一次绘制item 的时候显示动画
     */
    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    /**
     * 必须实现的方法，helper用来将item的数据显示到对应的View上
     */
    protected abstract void convert(K helper, T item);

    /**
     * 获取指定位置ID的View
     */
    public View getViewByPosition(int position, int viewId) {
        checkNotNull();
        return getViewByPosition(getRecyclerView(), position, viewId);
    }

    public View getViewByPosition(RecyclerView recyclerView, int position, int viewId) {
        if (recyclerView == null) {
            return null;
        }
        BaseViewHolder viewHolder = (BaseViewHolder) recyclerView.findViewHolderForLayoutPosition(position);
        return viewHolder.getView(viewId);
    }

    /**
     * 返回当前Item在list中的id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    private int recursiveExpand(int position, @NonNull List list) {
        int count = 0;
        int pos = position + list.size() - 1;
        for (int i = list.size() - 1; i >= 0; i--, pos--) {
            if (list.get(i) instanceof IExpandable) {
                IExpandable item = (IExpandable) list.get(i);
                if (item.isExpanded() && hasSubItems(item)) {
                    List subList = item.getSubItems();
                    mData.addAll(pos + 1, subList);
                    int subItemCount = recursiveExpand(pos + 1, subList);
                    count += subItemCount;
                }
            }
        }
        return count;

    }

    /**
     * 展开一个可一展开的Item
     *
     * @param position     当前位置
     * @param animate      展开动画
     * @param shouldNotify 是否通知整个RecyclerView更新绘制
     * @return item所添加的展开item个数
     */
    public int expand(@IntRange(from = 0) int position, boolean animate, boolean shouldNotify) {
        position -= getHeaderLayoutCount();

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        if (!hasSubItems(expandable)) {
            expandable.setExpanded(false);
            return 0;
        }
        int subItemCount = 0;
        if (!expandable.isExpanded()) {
            List list = expandable.getSubItems();
            mData.addAll(position + 1, list);
            subItemCount += recursiveExpand(position + 1, list);

            expandable.setExpanded(true);
            subItemCount += list.size();
        }
        int parentPos = position + getHeaderLayoutCount();
        if (shouldNotify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeInserted(parentPos + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    /**
     *默认通知重新绘制，是否产生动画效果
     */
    public int expand(@IntRange(from = 0) int position, boolean animate) {
        return expand(position, animate, true);
    }

    /**
     * 通知重新绘制，动画效果
     */
    public int expand(@IntRange(from = 0) int position) {
        return expand(position, true, true);
    }

    public int expandAll(int position, boolean animate, boolean notify) {
        position -= getHeaderLayoutCount();

        T endItem = null;
        if (position + 1 < this.mData.size()) {
            endItem = getItem(position + 1);
        }

        IExpandable expandable = getExpandableItem(position);
        if (!hasSubItems(expandable)) {
            return 0;
        }

        int count = expand(position + getHeaderLayoutCount(), false, false);
        for (int i = position + 1; i < this.mData.size(); i++) {
            T item = getItem(i);

            if (item == endItem) {
                break;
            }
            if (isExpandable(item)) {
                count += expand(i + getHeaderLayoutCount(), false, false);
            }
        }

        if (notify) {
            if (animate) {
                notifyItemRangeInserted(position + getHeaderLayoutCount() + 1, count);
            } else {
                notifyDataSetChanged();
            }
        }
        return count;
    }

    /**
     *展开所有可展开的Item
     */
    public int expandAll(int position, boolean init) {
        return expandAll(position, true, !init);
    }

    private int recursiveCollapse(@IntRange(from = 0) int position) {
        T item = getItem(position);
        if (!isExpandable(item)) {
            return 0;
        }
        IExpandable expandable = (IExpandable) item;
        int subItemCount = 0;
        if (expandable.isExpanded()) {
            List<T> subItems = expandable.getSubItems();
            for (int i = subItems.size() - 1; i >= 0; i--) {
                T subItem = subItems.get(i);
                int pos = getItemPosition(subItem);
                if (pos < 0) {
                    continue;
                }
                if (subItem instanceof IExpandable) {
                    subItemCount += recursiveCollapse(pos);
                }
                mData.remove(pos);
                subItemCount++;
            }
        }
        return subItemCount;
    }

    /**
     收缩当前展开的Item
     */
    public int collapse(@IntRange(from = 0) int position, boolean animate, boolean notify) {
        position -= getHeaderLayoutCount();

        IExpandable expandable = getExpandableItem(position);
        if (expandable == null) {
            return 0;
        }
        int subItemCount = recursiveCollapse(position);
        expandable.setExpanded(false);
        int parentPos = position + getHeaderLayoutCount();
        if (notify) {
            if (animate) {
                notifyItemChanged(parentPos);
                notifyItemRangeRemoved(parentPos + 1, subItemCount);
            } else {
                notifyDataSetChanged();
            }
        }
        return subItemCount;
    }

    /**
     收缩当前展开的Item
     */
    public int collapse(@IntRange(from = 0) int position) {
        return collapse(position, true, true);
    }

    /**
     收缩当前展开的Item
     */
    public int collapse(@IntRange(from = 0) int position, boolean animate) {
        return collapse(position, animate, true);
    }

    private int getItemPosition(T item) {
        return item != null && mData != null && !mData.isEmpty() ? mData.indexOf(item) : -1;
    }

    private boolean hasSubItems(IExpandable item) {
        List list = item.getSubItems();
        return list != null && list.size() > 0;
    }

    public boolean isExpandable(T item) {
        return item != null && item instanceof IExpandable;
    }

    private IExpandable getExpandableItem(int position) {
        T item = getItem(position);
        if (isExpandable(item)) {
            return (IExpandable) item;
        } else {
            return null;
        }
    }

    /**
     * 返回当前Item上一级所处位置，根据几级菜单来决定
     */
    public int getParentPosition(@NonNull T item) {
        int position = getItemPosition(item);
        if (position == -1) {
            return -1;
        }

        int level;
        if (item instanceof IExpandable) {
            level = ((IExpandable) item).getLevel();
        } else {
            level = Integer.MAX_VALUE;
        }
        if (level == 0) {
            return position;
        } else if (level == -1) {
            return -1;
        }

        for (int i = position; i >= 0; i--) {
            T temp = mData.get(i);
            if (temp instanceof IExpandable) {
                IExpandable expandable = (IExpandable) temp;
                if (expandable.getLevel() >= 0 && expandable.getLevel() < level) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * item中子view的点击事件
     */
    public interface OnItemChildClickListener {
        boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position);
    }


    /**
     * item中子view的长按事件
     */
    public interface OnItemChildLongClickListener {
        void onItemChildLongClick(BaseQuickAdapter adapter, View view, int position);
    }

    /**
     * Item本身的长按事件
     */
    public interface OnItemLongClickListener {
        boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position);
    }


    /**
     * Item本身的点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(BaseQuickAdapter adapter, View view, int position);
    }

    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        mOnItemChildClickListener = listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener listener) {
        mOnItemChildLongClickListener = listener;
    }
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }
    @Nullable
    public final OnItemChildClickListener getOnItemChildClickListener() {
        return mOnItemChildClickListener;
    }
    @Nullable
    public final OnItemChildLongClickListener getmOnItemChildLongClickListener() {
        return mOnItemChildLongClickListener;
    }
}
