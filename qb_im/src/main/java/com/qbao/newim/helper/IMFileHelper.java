package com.qbao.newim.helper;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.configure.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenjian on 2017/3/27.
 */

public class IMFileHelper {
    public static List<String> IMAGE_SUFFIX_LIST = Collections
            .synchronizedList(new ArrayList<String>());

    public static List<String> AUDIO_SUFFIX_LIST = Collections
            .synchronizedList(new ArrayList<String>());

    static {
        IMAGE_SUFFIX_LIST.add("gif");
        IMAGE_SUFFIX_LIST.add("png");
        IMAGE_SUFFIX_LIST.add("jpeg");
        IMAGE_SUFFIX_LIST.add("tiff");
        IMAGE_SUFFIX_LIST.add("jpg");
        IMAGE_SUFFIX_LIST.add("bmp");
        IMAGE_SUFFIX_LIST.add("img");
    }

    static {
        AUDIO_SUFFIX_LIST.add("basic");
        AUDIO_SUFFIX_LIST.add("x-wav");
        AUDIO_SUFFIX_LIST.add("x-mpeg");
        AUDIO_SUFFIX_LIST.add("x-mpeg-2");
        AUDIO_SUFFIX_LIST.add("mp3");
        AUDIO_SUFFIX_LIST.add("amr");
        AUDIO_SUFFIX_LIST.add("aac");
        AUDIO_SUFFIX_LIST.add("au");
    }

    public static int judgeFileType(String fileSuffix) {
        fileSuffix = fileSuffix.substring(fileSuffix.indexOf(".") + 1);
        int fileType = MsgConstDef.MSG_M_TYPE.TEXT;
        if (IMAGE_SUFFIX_LIST.contains(fileSuffix)) {
            fileType = MsgConstDef.MSG_M_TYPE.IMAGE;
        } else if (AUDIO_SUFFIX_LIST.contains(fileSuffix)) {
            fileType = MsgConstDef.MSG_M_TYPE.VOICE;
        }
        return fileType;
    }

    public static String generateFilePath(String fileUrl, String fileName) {
        int fileType = judgeFileType(fileName);
        String filePath = generateFilePath(fileType, fileUrl, fileName);
        return filePath;
    }

    public static String generateFilePath(int fileType, String fileUrl, String fileName) {
        String filePath;
        if (fileType == MsgConstDef.MSG_M_TYPE.IMAGE) {
            if (fileUrl.contains("thumnail=1")) {
                filePath = Constants.ICON_THUMB_DIR + "/" + fileName;
            } else {
                filePath = Constants.ICON_PIC_DIR + "/" + fileName;
            }
        } else if (fileType == MsgConstDef.MSG_M_TYPE.VOICE) {
            filePath = Constants.AUDIO_CACHE_DIR + "/" + fileName;
        } else {
            filePath = Constants.BASE_PATH + "/" + fileName;
        }
        return filePath;
    }
}
