package com.qbao.newim.util;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.qbao.newim.qbim.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表情工具类
 * 
 * @author zhangxiaolong
 * @since qianbao1.1
 */
public class FaceUtil
{
	/**
	 * 表情显示方式 列表中、textview中、edittext中
	 * 
	 * @author zhangxiaolong
	 * @since qianbao1.1
	 */
	public enum FACE_TYPE
	{
		/*** 列表中的表情 */
		LIST_TEXTVIEW,
		
		/*** 聊天界面列表中的表情 */
		CHAT_TEXTVIEW,
		
		/*** 输入框中的表情 */
		CHAT_EDITTEXT,
		CHAT_QB_FACE,
		RAW_SIZE
	}
	
	private final String TAG = FaceUtil.class.getSimpleName();
	
	/**
	 * 表情缓存map key 前缀
	 */
	private static final String KEY_PRE = "face_";
	
	private static FaceUtil instance;
	
	private Resources mResources = null;
	private Pattern pattern = null;
	
	/**
	 * 表情缓存
	 */
	private ImageCache imageCache;
	
	/**
	 * 表情编码
	 */
	private List<String> mFaceCodes = new ArrayList<String>();
	
	/**
	 * 表情编码和图片名称映射
	 */
	private HashMap<String, String> faceCodeToNameMap = new HashMap<String, String>();
	
//	private int faceListSize;
//	private int faceChatSize;
//	private int qbfaceChatSize;

	private FaceUtil()
	{
		mResources = AppUtil.GetContext().getApplicationContext().getResources();
		imageCache = new ImageCache();
//		faceListSize = (int)mResources.getDimensionPixelOffset(R.dimen.face_list_size);
//		faceChatSize = (int)mResources.getDimensionPixelOffset(R.dimen.face_chat_size);
//		qbfaceChatSize = (int)mResources.getDimensionPixelOffset(R.dimen.qbface_chat_size);
		
		initFace();
	}

	public synchronized static FaceUtil getInstance()
	{
		if (null == instance)
	    {
		    instance = new FaceUtil();
	    }
		return instance;
	}
	
	/**
	 * 加载表情
	 * void
	 */
	private void initFace()
	{
	    String[] faceCodes = mResources.getStringArray(R.array.nim_face_code);
	    String[] faceNames = mResources.getStringArray(R.array.nim_face_name);
	    
	    int length = faceCodes.length;
        for(int i = 0 ; i < length ; i ++ )
        {
        	String faceCode = faceCodes[i];
        	mFaceCodes.add(faceCode);
        	faceCodeToNameMap.put(faceCode, faceNames[i]);
        }
        
//        faceCodes = mResources.getStringArray(R.array.qb_face_code);
//        faceNames = mResources.getStringArray(R.array.qb_face_name);
//	    length = faceCodes.length;
//        for(int i = 0 ; i < length ; i ++ )
//        {
//        	String faceCode = faceCodes[i];
//        	mFaceCodes.add(faceCode);
//        	faceCodeToNameMap.put(faceCode, faceNames[i]);
//        }
        
        addOtherCode();
        
        pattern = buildPattern();
	}
	
	private void addOtherCode()
	{
		mFaceCodes.add("￼");
    	faceCodeToNameMap.put("￼", "web_link_icon");
	}
	
	private Pattern buildPattern()
	{
		if(mFaceCodes == null || mFaceCodes.isEmpty())
		{
			return Pattern.compile("");
		}
		
		StringBuilder patternString = new StringBuilder();
		
		try
		{
			patternString.append('(');
	
			for (String s : mFaceCodes)
			{
				patternString.append(Pattern.quote(s));
				patternString.append('|');
			}
	
			patternString.replace(patternString.length() - 1, patternString.length(), ")");
		
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return Pattern.compile(patternString.toString());
	}

	public CharSequence formatTextToFace(CharSequence text , FACE_TYPE faceType)
	{
		return formatTextToFace(text, faceType, -1);
	}
	
	/**
	 * 转换成表情
	 * 
	 * @param text 原始文本
	 * @param faceType 表情大小
	 * @param preCount 只转换前面多少个字符
	 * @return
	 */
	public CharSequence formatTextToFace(CharSequence text, FACE_TYPE faceType, int preCount)
	{
		if(text == null)
		{
			text = "";
		}
		
		if(preCount > 0 && preCount <= text.length())
		{
			text = text.subSequence(0, preCount);
		}
		
		final SpannableStringBuilder builder = new SpannableStringBuilder(text);
		final Matcher matcher = pattern.matcher(text);

        String faceText;
        String drawableName;
		while (matcher.find())
		{
			faceText = matcher.group();
			drawableName = faceCodeToNameMap.get(faceText);
			
			ImageSpan imageSpan = createImageSpan(drawableName, getFactor(drawableName, faceType));
			if(imageSpan != null)
			{
				builder.setSpan(imageSpan, matcher.start(),
						matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		return builder;
	}

	private ImageSpan createImageSpan(String drawableName, float factor)
	{
		drawableName = drawableName.toLowerCase();
		
		final String key = KEY_PRE + drawableName;
		DrawableCache drawableCache = imageCache.get(key);
		
		if(drawableCache != null)
		{
			return new FaceCenteredImageSpan(drawableCache.drawableReference.get(), factor);
		}
		
		try
		{
			final Field f = (Field) R.drawable.class.getDeclaredField(drawableName);
			final int id = f.getInt(R.drawable.class);
			final Drawable drawable = mResources.getDrawable(id);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			
			drawableCache = new DrawableCache(drawable);
			imageCache.put(key, drawableCache);
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

        return new FaceCenteredImageSpan(drawableCache.drawableReference.get(), factor);
	}

    private float getFactor(String drawableName, FACE_TYPE faceType)
    {
        if(FACE_TYPE.CHAT_QB_FACE == faceType ||  "web_link_icon".equals(drawableName))
        {
            return 1.0f;
        }
        else
        {
            return 1.2f;
        }
    }

	/**
	 * 重新加载表情
	 * void
	 */
	public void reLoadFace() 
	{
	    initFace();
	}
	
	/**
	 * 判断是不是表情
	 * 
	 * @param string 原文本
	 * @return 是返回true 不是返回false
	 */
	public boolean isFace(CharSequence string)
	{
		if(TextUtils.isEmpty(string))
		{
			return false;
		}
		
		Matcher matcher = pattern.matcher(string);
		
		if (matcher.find())
		{
			return true;
		}
		
		return false;
	}
	
    public void clearFaceBitmap()
	{
		imageCache.clear();
	}
    
	private static class ImageCache extends WeakHashMap<String, DrawableCache> 
	{
		private static final long serialVersionUID = 1L;
		
		public boolean isCached(String url){
			return containsKey(url) && get(url) != null;
		}
	}
	
	private static class DrawableCache
	{
		WeakReference<Drawable> drawableReference;
		FACE_TYPE faceType;
		int size;

        public DrawableCache(Drawable drawable)
        {
            drawableReference = new WeakReference<Drawable>(drawable);
        }

		public DrawableCache(Drawable drawable, FACE_TYPE faceType, int size)
		{
			drawableReference = new WeakReference<Drawable>(drawable);
			this.faceType = faceType;
			this.size = size;
		}
	}


    private static class FaceCenteredImageSpan extends ImageSpan {

        private float factor;
        public FaceCenteredImageSpan(Drawable d, float factor) {
            this(d);
            this.factor = factor;
        }

        public FaceCenteredImageSpan(Drawable d) {
            super(d);
            this.factor = 1.0f;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            Drawable drawable = getCachedDrawable();
            drawable.setBounds(0, 0, (int)(paint.getTextSize() * factor), (int)(paint.getTextSize() * factor));
            canvas.save();
            int transY = 0;
            transY = ((bottom - top) - drawable.getBounds().bottom) / 2 + top;
            canvas.translate(x, transY);

            drawable.draw(canvas);
            canvas.restore();
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end,
                           Paint.FontMetricsInt fontMetricsInt) {
            Drawable drawable = getCachedDrawable();
            drawable.setBounds(0, 0, (int)(paint.getTextSize() * factor), (int)(paint.getTextSize() * factor));
            Rect rect = drawable.getBounds();
            if (fontMetricsInt != null) {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                fontMetricsInt.ascent = fmPaint.ascent;
                fontMetricsInt.descent = fmPaint.descent;
                fontMetricsInt.top = fmPaint.top;
                fontMetricsInt.bottom = fmPaint.bottom;
            }
            return rect.right;
        }

        private WeakReference<Drawable> mDrawableRef;
        private Drawable getCachedDrawable() {
            WeakReference<Drawable> wr = mDrawableRef;
            Drawable d = null;

            if (wr != null)
                d = wr.get();

            if (d == null) {
                d = getDrawable();
                mDrawableRef = new WeakReference<Drawable>(d);
            }

            return d;
        }
    }
}
