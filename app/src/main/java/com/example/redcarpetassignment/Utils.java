package com.example.redcarpetassignment;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Utils {
    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
    public static void showSnackbar(Context context,String message){
        Snackbar.make(((Activity)context).findViewById(R.id.content),message,Snackbar.LENGTH_SHORT).show();
    }
}
