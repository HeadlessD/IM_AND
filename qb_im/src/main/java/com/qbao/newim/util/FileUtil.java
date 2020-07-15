package com.qbao.newim.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import com.qbao.newim.configure.Constants;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.qbim.R;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by chenjian on 2017/3/27.
 */

public class FileUtil {
    public static final String IMAGE_SUFFIX_JPG = ".jpg";

    /**
     * 判断SD卡是否存在
     */
    public static boolean hasSdcard()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * 检查是否有足够的存储空间
     *
     * @param path 文件路径
     * @param needSize 所需空间
     * @return
     */
    public static boolean hasFreeSpace(String path, long needSize)
    {
        if (TextUtils.isEmpty(path))
        {
            return true;
        }

        long freeSpace = 0;

        if (path.startsWith(Environment.getRootDirectory().getAbsolutePath()))
        {
            ///system 是否需要先判断该分区是否可写
            freeSpace = getFreeSpaceOfInternalStorage();
        }
        else if (path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath()))
        {
            ///mnt/sdcard
            freeSpace = getFreeSpaceOfExternalStorage();
        }
        else if (path.startsWith(Environment.getDataDirectory().getAbsolutePath()))
        {
            ///data
            freeSpace = getFreeSpaceOfAppData();
        }
        else
        {
            freeSpace = 0;
        }

        return freeSpace >= needSize;
    }

    /**
     * 获取获取指定目录剩余空间大小 返回值单位字节
     *
     * @return
     */
    public static long getFreeSpaceOfPath(File fileDir)
    {
        StatFs sf = new StatFs(fileDir.getPath());
        long blockSize = sf.getBlockSize();
        long availableCount = sf.getAvailableBlocks();
        long freeSpace = availableCount * blockSize;

        return freeSpace;
    }

    /**
     * 获取外部存储剩余空间大小 (单位字节)
     *
     * @return
     */
    public static long getFreeSpaceOfExternalStorage()
    {
        File sdcardDir = Environment.getExternalStorageDirectory();
        return getFreeSpaceOfPath(sdcardDir);
    }

    /**
     * 获取内部存储剩余空间大小 (单位字节)
     *
     * @return
     */
    public static long getFreeSpaceOfInternalStorage()
    {
        // 获取 Android 的根目录/system
        File rootDir = Environment.getRootDirectory();
        return getFreeSpaceOfPath(rootDir);
    }

    /**
     * 获取应用数据目录剩余空间大小 (单位字节)
     *
     * @return
     */
    public static long getFreeSpaceOfAppData()
    {
        //获取 Android 数据目录。
        File dataDir = Environment.getDataDirectory();
        return getFreeSpaceOfPath(dataDir);
    }

    /**
     * 格式化网络文件名
     *
     * @param url
     *            网络url
     * @param fileSuffix
     *            文件后缀名
     * @return
     */
    public static String convertUrlToFileName(String url, String fileSuffix)
    {

        if (TextUtils.isEmpty(url))
        {
            return "";
        }

        String filename = hashKeyForDisk(url) + fileSuffix;
        return filename;
    }

    /**
     * 确保应用根目录已经创建
     *
     * @param uri
     * @return
     */
    public static boolean ensureAppPath(String uri)
    {
        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        if ((dataDir).equals(uri.substring(0, 5)))
        {
            File file = new File(uri);
            if (!file.exists())
            {
                boolean success = file.mkdirs();
                if (!success)
                {
                    return false;
                }
            }
        }
        else
        {
            String sdcardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (new File(sdcardDir).canRead())
            {
                File file = new File(Constants.BASE_PATH);
                if (!file.exists())
                {
                    boolean success = file.mkdirs();
                    if (!success)
                    {
                        return false;
                    }
                }

                file = new File(uri);
                if (!file.exists())
                {
                    boolean success = file.mkdirs();
                    if (!success)
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 获取唯一的文件名
     *
     * @param type
     * @param tempName
     * @return
     */
    public static String getUniqueName(String type, String tempName)
    {
        int i = new Random().nextInt(10000000);
        StringBuilder newName = new StringBuilder();
        newName.append(tempName);
        newName.append("_");
        newName.append(i);
        newName.append(type);

        while (true)
        {
            File file1 = new File(Constants.ICON_PIC_DIR + "/" + newName.toString());
            File file2 = new File(Constants.ICON_THUMB_DIR + "/" + newName.toString());
            if (file1.exists() || file2.exists())
            {
                i++;
                newName = new StringBuilder();
                newName.append(tempName);
                newName.append("_");
                newName.append(i);
                newName.append(type);
            }
            else
            {
                break;
            }
        }
        return newName.toString();
    }

    public static String hashKeyForDisk(String key)
    {
        String cacheKey;
        try
        {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes)
    {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++)
        {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
            {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void saveImageToSdcard(ImageView imageView) {
        Context context = AppUtil.GetContext();
        if (AndPermission.hasPermission(context, WRITE_EXTERNAL_STORAGE)) {
            imageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
            imageView.setDrawingCacheEnabled(false);
            String uploadFilePath = Constants.ICON_PIC_DIR + "/"  + "nim_export" + System.currentTimeMillis();
            File uploadFile = new File(uploadFilePath);
            if(uploadFile.exists()) {
                return ;
            }
            ensureAppPath(Constants.ICON_PIC_DIR);
            boolean success = BitmapUtil.saveBitmapToSD(uploadFilePath, bitmap);
            MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, uploadFilePath, "qianbao");
            if (success) {
                ShowUtils.showToast(context.getResources().getString(R.string.nim_image_save_path, Constants.ICON_PIC_DIR));
            }
        }
    }
}
