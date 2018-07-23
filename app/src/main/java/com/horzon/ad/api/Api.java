package com.horzon.ad.api;

import com.android.pc.ioc.inject.InjectHttpOk;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.horzon.ad.model.ResouceModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.horzon.utils.LogUtil;
import com.horzon.utils.MD5Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.realm.Realm;

public class Api {


    private static final String TAG = "Api";

    private static final int KEY_CHECK_VERSION = 1;

    public final Gson gson = new Gson();

    public boolean downloadFile(String strURL, String nameSaved, String md5) {
        if (strURL == null || nameSaved == null || md5 == null) {
            LogUtil.e(TAG, "downloadFile: strURL == null || nameSaved == null || md5 == null");
            return false;
        }
        long startPosition = 0;
        long endPosition = 0;

        File fileSaved = new File(nameSaved);
        if (fileSaved.exists()) {
            LogUtil.e(TAG, "downloadFile: " + nameSaved + " is existed, no need to download!");
            return true;
        }
        if (!fileSaved.getParentFile().exists()) {
            LogUtil.e(TAG, fileSaved.getParentFile().getPath() + " is not existed, mkdir : " + fileSaved.getParentFile().mkdir());
        }


        HttpURLConnection conn = null;
        String pathFile = nameSaved + ".downloading";
        File fileDownloading = new File(pathFile);
        try {
            URL url = new URL(strURL);
            LogUtil.d(TAG, "downloadFile: strUrl = " + strURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            long length = conn.getContentLength();
            LogUtil.d(TAG, "downloadFile: length = " + length);
            if (fileDownloading.exists()) {
                // 如果存在，则重新建立连接，断点下载
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }
                // 往前偏移10k，这样防止传输的准确性
                startPosition = fileDownloading.length() - 1024 * 10;
                if (startPosition < 0) {
                    startPosition = 0;
                }
                endPosition = length;
                LogUtil.d(TAG, "downloadFile: startPosition = " + startPosition + ", endPosition = " + endPosition);
                url = new URL(strURL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // 设置开始下载的位置和结束下载的位置，单位为字节
                conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + (endPosition - 1));
                conn.connect();
                length = conn.getContentLength();
                LogUtil.d(TAG, "downloadFile: reconnect,length = " + length);
            } else {
                // 如果不存在，则继续下载。
            }
            RandomAccessFile access = new RandomAccessFile(fileDownloading, "rw");
            // 移动指针到开始位置
            access.seek(startPosition);
            LogUtil.d(TAG, "downloadFile: getResponseCode = " + conn.getResponseCode());
            if ((conn.getResponseCode() == 206) || (conn.getResponseCode() == 200)) {
                long total = 0;
                byte buf[] = new byte[1024];
                int downloadROMProgress = -1;
                InputStream is = conn.getInputStream();
                int numread = 0;
                while ((numread = is.read(buf)) >= 0) {
                    access.write(buf, 0, numread);
                    total += numread;
                    int apk_progress = (int) (((float) total / length) * 100);
                    if (apk_progress > downloadROMProgress) {
                        LogUtil.d(TAG, "downloadFile: apk_progress = " + apk_progress);
                        downloadROMProgress = apk_progress;
                    }
                }
                LogUtil.d(TAG, "downloadFile: total = " + total);
                // 关闭
                if (access != null) {
                    access.close();
                    access = null;
                }
                if (is != null) {
                    is.close();
                    is = null;
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            return false;
        } finally {
            if (conn != null) {
                LogUtil.d(TAG, "downloadFile: before disconnect");
                conn.disconnect();
                conn = null;
            }
        }

        File fileDownloaded = new File(pathFile);
        if (fileDownloaded.exists()) {
            String md5Local = MD5Util.getFileMD5(fileDownloaded, null);
            if (md5Local.equals(md5)) {
                LogUtil.d(TAG, "downloadFile: MD5 check correct, rename to " + nameSaved);
                // 修改为正常的名字
                fileDownloaded.renameTo(fileSaved);
                return true;
            } else {
                LogUtil.e(TAG, "downloadFile: " + pathFile + " is not correct");
                fileDownloaded.delete();
            }
        } else {
            LogUtil.d(TAG, "downloadFile: " + pathFile + " is not existed");
        }
        return false;
    }


    public void checkVersion() {
        LogUtil.e(TAG, "checkVersion");
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();


        try {
            JSONObject data = new JSONObject();
            data.put("imei", "123");
            data.put("sh_name_id", "10007");
            data.put("customer_id", "35");
            data.put("dev_model_id", "32");
            data.put("software_version", "1");
            data.put("hardware_version", "1");

            params.put("update_manager", data.toString());
            InternetConfig config = new InternetConfig();
            config.setKey(KEY_CHECK_VERSION);
            FastHttpHander
                    .ajax("http://47.92.2.131/admin/client_interact_apiv1.php?action=resouce_upgrade_check",
                            params, config, this);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            LogUtil.e(TAG, e);
        }

    }


    @InjectHttpOk
    private void onSuccess(ResponseEntity entity) {
        LogUtil.d(TAG, "onSuccess:" + entity);


        if (entity.getKey() == KEY_CHECK_VERSION) {
            Type type = new TypeToken<ArrayList<ResouceModel>>() {
            }.getType();
            final ArrayList<ResouceModel> models = new Gson().fromJson(entity.getContentAsString(), type);


            LogUtil.d(TAG, "models:" + models);

            new Thread() {
                @Override
                public void run() {


                    for (ResouceModel model : models) {

                        parseResource(model);


                    }
                }
            }.start();


        }


    }

    public void parseResource(ResouceModel model) {
        Realm realm = Realm.getDefaultInstance();
        ResouceModel currentModel = realm.where(ResouceModel.class).equalTo("package_id", model.getPackage_id()).findFirst();


        String filePath = "/mnt/sdcard/ad/" + model.getPackage_file();
        boolean flag = downloadFile(model.getOss_ota_path(), "/mnt/sdcard/ad/" + model.getPackage_file(), model.getMd5_file());

        realm.beginTransaction();

        model.setFile_path(filePath);
        realm.copyToRealmOrUpdate(model);


        realm.commitTransaction();
    }


    public void checkFile() {

        //检查有没有需要下载的文件


        // 在本地生成一个 file.xml ,来保存 本地的文件.


    }


}
