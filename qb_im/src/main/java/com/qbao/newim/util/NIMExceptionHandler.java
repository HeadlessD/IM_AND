package com.qbao.newim.util;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import com.qbao.newim.configure.Constants;
import com.qbao.newim.qbim.R;

/**
 * Created by shiyunjie on 2017/10/9.
 */

//crash捕获类
public class NIMExceptionHandler implements Thread.UncaughtExceptionHandler
{
    public static final String TAG = "FTExceptionHandler";

    private Thread.UncaughtExceptionHandler m_default_e_handler;
    private static NIMExceptionHandler m_instance;
    private Context m_context;

    private Properties m_p_crash = new Properties();
    private static final String STACK_TRACE = "STACK_TRACE";

    private static final String CRASH_REPORTER_EXTENSION = ".crash";


//    private  boolean m_write_file = false;

    private NIMExceptionHandler() {}
    public static NIMExceptionHandler getInstance()
    {
        if (m_instance == null) {
            m_instance = new NIMExceptionHandler();
        }
        return m_instance;
    }

    public void init(Context context)
    {
        m_context = context;
        m_default_e_handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    public void uncaughtException(Thread thread, Throwable ex)
    {
        if (!handleException(ex) && m_default_e_handler != null)
        {
            m_default_e_handler.uncaughtException(thread, ex);
        }
        else
        {
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                Logger.error(TAG, "Error : " + e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }


    private boolean handleException(Throwable ex)
    {
        if (ex == null)
        {
            return true;
        }

        new Thread()
        {
            public void run()
            {
                Looper.prepare();
                String lstrNotify = m_context.getResources().getString(R.string.nim_exception_notify);
                Toast.makeText(m_context, lstrNotify, Toast.LENGTH_LONG).show();
                Looper.loop();
            }

        }.start();

        saveCrashInfoToFile(ex);


//        new Thread()
//        {
//            public void run()
//            {
//                m_write_file = false;
//                Logger.debug(TAG, "write file");
//                //mCrashStackInfo.toString()
//                m_write_file = true;
//            }
//
//        }.start();
//
//
//        while(!m_write_file)
//        {
//            try
//            {
//                Thread.sleep(1000);
//            }
//            catch (InterruptedException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        return true;
    }

    private String saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null)
        {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        String result = info.toString();
        printWriter.close();
//        m_p_crash.put(STACK_TRACE, result);

        try
        {
            long timestamp = System.currentTimeMillis();
            String file_name = "crash-" + timestamp + CRASH_REPORTER_EXTENSION;
            File file = new File(Constants.BASE_PATH, file_name);
            OutputStream output_stream = new FileOutputStream(file);
            output_stream.write(result.getBytes());
            output_stream.flush();
            output_stream.close();
            return file_name;
        }
        catch (Exception e)
        {
            Logger.error(TAG, "an error occured while writing report file..." + e);
        }
        return null;
    }
}
