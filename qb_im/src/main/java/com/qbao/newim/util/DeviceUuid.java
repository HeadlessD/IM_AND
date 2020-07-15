package com.qbao.newim.util;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.qbao.newim.helper.AES;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import static com.qbao.newim.configure.Constants.DATA_CACHE_DIR;

/**
 * Created by fangda on 2016/11/28.
 * 设备唯一标识生成类
 */

public class DeviceUuid {
    private static final String ENCRYPTO_KEY = "8afh4waoW7du32fx";
    private static final String SALT = android.os.Build.MODEL;
    private static final String UUID_FILENAME = DATA_CACHE_DIR + "/device";

    private static String uuid;
    private static DeviceUuid deviceUuid;

    public static DeviceUuid getInstance() {
        if (deviceUuid == null) {
            synchronized (DeviceUuid.class) {
                if (deviceUuid == null) {
                    deviceUuid = new DeviceUuid();
                }
            }
        }
        return deviceUuid;
    }

    private DeviceUuid() {
    }


    public String getDeviceUuid(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuid.class) {
                if (uuid == null) {
                    byte[] readbytes = readFileInBytesToString(UUID_FILENAME);
                    uuid = checkValid(readbytes);
                    if (uuid == null) {
                        generateUuid(context);
                        writeToFile();
                    }
                }
            }
        }
        return uuid;
    }

    private void writeToFile() {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            String generateMd5 = new String(digest.digest((uuid.concat(SALT)).getBytes("UTF-8")), "UTF-8");
            String encryptedString = AES.Encrypt(uuid.concat(":").concat(generateMd5), ENCRYPTO_KEY);
            byte[] bytes = encryptedString.getBytes("UTF-8");
            File outfile = new File(UUID_FILENAME);
            File folder = new File(DATA_CACHE_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if (!outfile.exists()) {
                outfile.createNewFile();
            }
            DataOutputStream fw = new DataOutputStream(new FileOutputStream(
                    outfile));
            fw.write(bytes);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateUuid(Context context) {
        String tmDevice = null, tmSerial = null, androidId;
        boolean fetchTelephonyFailure = false, fetchAndroidIdFailue = false;
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            tmDevice = tm.getDeviceId();
            tmSerial = tm.getSimSerialNumber();
        } catch (SecurityException e) {
            e.printStackTrace();
            fetchTelephonyFailure = true;
        }
        androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(androidId) || "9774d56d682e549c".equals(androidId)) {
            fetchAndroidIdFailue = true;
        }
        if (fetchTelephonyFailure && fetchAndroidIdFailue) uuid = UUID.randomUUID().toString();
        else {
            tmDevice = tmDevice == null ? "" : tmDevice;
            tmSerial = tmSerial == null ? "" : tmSerial;
            uuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode()).toString();
        }
    }

    private String checkValid(byte[] readbytes) {
        try {
            String storgeString = AES.Decrypt(new String(readbytes, "UTF-8"), ENCRYPTO_KEY);
            int index = 0;
            if (storgeString != null) index = storgeString.indexOf(':');
            if (index > 0) {
                String uuid = storgeString.substring(0, index);
                String md5 = storgeString.substring(index + 1);
                try {
                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    if (!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(md5)) {
                        String checkedString = new String(digest.digest((uuid.concat(SALT)).getBytes("UTF-8")), "UTF-8");
                        if (md5.equals(checkedString)) return uuid;
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;

    }

    public byte[] readFileInBytesToString(String filePath) {
        File file = new File(filePath);

        final int readArraySizePerRead = 4096;
        ArrayList<Byte> bytes = new ArrayList<>();
        try {
            if (file.exists()) {
                DataInputStream isr = new DataInputStream(new FileInputStream(
                        file));
                byte[] tempchars = new byte[readArraySizePerRead];
                int charsReadCount;
                while ((charsReadCount = isr.read(tempchars)) != -1) {
                    for(int i = 0 ; i < charsReadCount ; i++){
                        bytes.add (tempchars[i]);
                    }
                }
                isr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return toPrimitives(bytes.toArray(new Byte[0]));
    }

    private byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }

}