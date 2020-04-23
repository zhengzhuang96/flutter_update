package com.rain.flutter_update;

import android.content.Context;

import androidx.annotation.NonNull;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import com.king.app.updater.AppUpdater;
import com.king.app.updater.callback.UpdateCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/** FlutterUpdatePlugin */
public class FlutterUpdatePlugin implements FlutterPlugin, EventChannel.StreamHandler {

  private static Context mContext;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    mContext = flutterPluginBinding.getApplicationContext();
    final EventChannel channel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "plugins.iwubida.com/update_version");
    channel.setStreamHandler(this);
  }


  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  @Override
  public void onListen(Object arguments, final EventChannel.EventSink events) {
    if (arguments.toString().length() < 5) {
      events.error(TAG, "URL错误", arguments);
      return;
    }
    if (!arguments.toString().startsWith("http")) {
      events.error(TAG, "URL错误", arguments);
    }
    AppUpdater update = new AppUpdater(mContext, arguments.toString()).setUpdateCallback(new UpdateCallback() {
      Map data = new HashMap<String, Object>();

      // 发送数据到 Flutter
      private void sendData() {
        events.success(data);
      }

      @Override
      public void onDownloading(boolean isDownloading) {

      }

      @Override
      public void onStart(String url) {
        data.put("start", true);
        data.put("cancel", true);
        data.put("done", true);
        data.put("error", false);
        data.put("percent", 1);
        sendData();
      }

      @Override
      public void onProgress(int progress, int total, boolean isChange) {
        int percent = (int)(progress * 1.0 / total * 100);
        if (isChange && percent > 0) {
          data.put("percent", percent);
          sendData();
        }
      }

      @Override
      public void onFinish(File file) {
        data.put("done", true);
        sendData();
      }

      @Override
      public void onError(Exception e) {
        data.put("error", e.toString());
        sendData();
      }

      @Override
      public void onCancel() {
        data.put("cancel", true);
        sendData();
      }
    });
    update.start();
  }


  @Override
  public void onCancel(Object arguments) {
    Log.i(TAG, "取消下载-集成的第三方下载没有提供取消方法");
  }
}
