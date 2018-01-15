package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.MimeTypes;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.util.Locale;

/** Review commets：
 * 使用一个工具类来放置一些常用的函数方法非常棒！更进一步，我们还可以将其构造方法私有化来防止被外部实例化对象
 * 就是加一个关键字：final
 * public final class Utility{
    private Utility(){}
    }
 * Created by gaochuang on 2017/10/26.
 */


public final class Utility {
    private Utility(){}

    public static boolean checkNetworkStatus(Context con){
        ConnectivityManager connMgr = (ConnectivityManager)con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(wifiInfo.getState() == NetworkInfo.State.CONNECTED || mobileInfo.getState() == NetworkInfo.State.CONNECTED){
            return true;
        }else{
            return false;
        }
    }

    /**
     *
     * @param applicationContext 通过context获取AssetManager，从而可以打开Assets目录下的文件
     * @param fileName 要读取的xml文件名称
     * @param nodeName 要获取的节点名称。
     * @return
     */
    public static String getServerIP(Context applicationContext,  String fileName, String nodeName) {
        String serverIP = null;
        XmlPullParser xpp;
        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            xpp.setInput(applicationContext.getAssets().open(fileName),null);
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && (xpp.getName().equalsIgnoreCase(nodeName))){ //parse the wanted tag
                    Log.i("config.xml parsing", "getName = "+ xpp.getName());
                    Log.i("config.xml parsing", "getText = "+ xpp.getText());
                    serverIP = xpp.nextText();
                    break;
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverIP;
    }

    /**
     * Builds a track name for display.
     *
     * @param format {@link Format} of the track.
     * @return a generated name specific to the track.
     */
    public static String buildTrackName(Format format) {
        String trackName;
        if (MimeTypes.isVideo(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(
                    buildResolutionString(format), buildBitrateString(format)), buildTrackIdString(format)),
                    buildSampleMimeTypeString(format));
        } else if (MimeTypes.isAudio(format.sampleMimeType)) {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(joinWithSeparator(
                    buildLanguageString(format), buildAudioPropertyString(format)),
                    buildBitrateString(format)), buildTrackIdString(format)),
                    buildSampleMimeTypeString(format));
        } else {
            trackName = joinWithSeparator(joinWithSeparator(joinWithSeparator(buildLanguageString(format),
                    buildBitrateString(format)), buildTrackIdString(format)),
                    buildSampleMimeTypeString(format));
        }
        return trackName.length() == 0 ? "unknown" : trackName;
    }


    private static String buildResolutionString(Format format) {
        return format.width == Format.NO_VALUE || format.height == Format.NO_VALUE
                ? "" : format.width + "x" + format.height;
    }

    private static String buildAudioPropertyString(Format format) {
        return format.channelCount == Format.NO_VALUE || format.sampleRate == Format.NO_VALUE
                ? "" : format.channelCount + "ch, " + format.sampleRate + "Hz";
    }

    private static String buildLanguageString(Format format) {
        return TextUtils.isEmpty(format.language) || "und".equals(format.language) ? ""
                : format.language;
    }

    private static String buildBitrateString(Format format) {
        return format.bitrate == Format.NO_VALUE ? ""
                : String.format(Locale.US, "%.2fMbit", format.bitrate / 1000000f);
    }

    private static String joinWithSeparator(String first, String second) {
        return first.length() == 0 ? second : (second.length() == 0 ? first : first + ", " + second);
    }

    private static String buildTrackIdString(Format format) {
        return format.id == null ? "" : ("id:" + format.id);
    }

    private static String buildSampleMimeTypeString(Format format) {
        return format.sampleMimeType == null ? "" : format.sampleMimeType;
    }
}
