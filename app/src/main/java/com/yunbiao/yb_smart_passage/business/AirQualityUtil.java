package com.yunbiao.yb_smart_passage.business;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.yunbiao.yb_smart_passage.afinel.ResourceUpdate;
import com.yunbiao.yb_smart_passage.utils.SpUtils;
import com.yunbiao.yb_smart_passage.utils.ThreadUitls;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

public class AirQualityUtil {
    private static final String TAG = "AirQualityUtil";
    private static String KEY = "airQuality";
    private static String DATE_KEY = "airDate";
    private static String url = ResourceUpdate.GET_AIRINFO;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public interface AirQualityCallback {
        void onCallback(AirQualityBean.SingleAirBean airBean);
    }

    public synchronized static void getAirQuality(final AirQualityCallback callback) {
        /*ThreadUitls.runInThread(new Runnable() {
            @Override
            public void run() {
                String response = SpUtils.getStr(KEY);
                String reqDate = SpUtils.getStr(DATE_KEY);
                String today = dateFormat.format(new Date());

                if (!TextUtils.isEmpty(response)) {
                    AirQualityBean singleAirBean = null;
                    try {
                        singleAirBean = new Gson().fromJson(response, AirQualityBean.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    AirQualityBean.SingleAirBean bean = null;

                    if (singleAirBean != null && singleAirBean.getData() != null && singleAirBean.getData().size() > 0) {
                        if (singleAirBean != null && singleAirBean.getData() != null && singleAirBean.getData().size() > 0) {
                            bean = singleAirBean.getData().get(0);
                        }
                    }

                    if (bean != null) {
                        if(TextUtils.equals(today,reqDate)){
                            if (callback != null) {
                                callback.onCallback(bean);
                            }
                            return;
                        }
//                        String insert_time = bean.getINSERT_TIME();
//                        String dataDate = null;
//                        try {
//                            Date date = dateFormat.parse(insert_time);
//                            dataDate = dateFormat.format(date);
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        Log.e(TAG, "run: ----- " + dataDate + " --- " + today);
//
//                        if (!TextUtils.isEmpty(dataDate) && TextUtils.equals(today, dataDate)) {
//                            if (callback != null) {
//                                callback.onCallback(bean);
//                            }
//                            return;
//                        }
                    }
                }

                OkHttpUtils.get().url(url)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e(TAG, "onError: -------- " + (e == null ? "NULL" : e.getMessage()));
                                if (callback != null) {
                                    callback.onCallback(null);
                                }
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.e(TAG, "onResponse: -------------- " + response);
                                AirQualityBean.SingleAirBean bean = null;
                                if (!TextUtils.isEmpty(response)) {
                                    SpUtils.saveStr(KEY, response);
                                    SpUtils.saveStr(DATE_KEY,dateFormat.format(new Date()));
                                    try {
                                        AirQualityBean singleAirBean = new Gson().fromJson(response, AirQualityBean.class);
                                        Log.e(TAG, "onResponse: ---- " + singleAirBean.toString());
                                        if (singleAirBean != null && singleAirBean.getData() != null && singleAirBean.getData().size() > 0) {
                                            bean = singleAirBean.getData().get(0);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (callback != null) {
                                    callback.onCallback(bean);
                                }

                            }

                            @Override
                            public void onAfter(int id) {

                            }
                        });
            }
        });*/
    }

    public class AirQualityBean {
        private List<SingleAirBean> data;

        public List<SingleAirBean> getData() {
            return data;
        }

        public void setData(List<SingleAirBean> data) {
            this.data = data;
        }

        public class SingleAirBean {
            private String AIR_LEVEL;//空气级别（1：蓝色；2：绿色；3：红色）
            private String CO2;//CO2
            private String DATE_TIME;//上传时间
            private String DEVICE_MAC;//设备MAC地址
            private String HCHO;//甲醛
            private String INSERT_TIME;//数据插入时间
            private String PM25;//室内PM2.5
            private String PM25_OUT;//室外PM2.5
            private String TEMPERATURE;//温度
            private String TVOC;//TVOC
            private String WETNESS;//湿度

            public void setAIR_LEVEL(String AIR_LEVEL) {
                this.AIR_LEVEL = AIR_LEVEL;
            }

            public void setCO2(String CO2) {
                this.CO2 = CO2;
            }

            public void setDATE_TIME(String DATE_TIME) {
                this.DATE_TIME = DATE_TIME;
            }

            public void setDEVICE_MAC(String DEVICE_MAC) {
                this.DEVICE_MAC = DEVICE_MAC;
            }

            public void setHCHO(String HCHO) {
                this.HCHO = HCHO;
            }

            public void setINSERT_TIME(String INSERT_TIME) {
                this.INSERT_TIME = INSERT_TIME;
            }

            public void setPM25(String PM25) {
                this.PM25 = PM25;
            }

            public void setPM25_OUT(String PM25_OUT) {
                this.PM25_OUT = PM25_OUT;
            }

            public void setTEMPERATURE(String TEMPERATURE) {
                this.TEMPERATURE = TEMPERATURE;
            }

            public void setTVOC(String TVOC) {
                this.TVOC = TVOC;
            }

            public void setWETNESS(String WETNESS) {
                this.WETNESS = WETNESS;
            }

            public String getAIR_LEVEL() {
                return AIR_LEVEL;
            }

            public String getCO2() {
                return CO2;
            }

            public String getDATE_TIME() {
                return DATE_TIME;
            }

            public String getDEVICE_MAC() {
                return DEVICE_MAC;
            }

            public String getHCHO() {
                return HCHO;
            }

            public String getINSERT_TIME() {
                return INSERT_TIME;
            }

            public String getPM25() {
                return PM25;
            }

            public String getPM25_OUT() {
                return PM25_OUT;
            }

            public String getTEMPERATURE() {
                return TEMPERATURE;
            }

            public String getTVOC() {
                return TVOC;
            }

            public String getWETNESS() {
                return WETNESS;
            }

            @Override
            public String toString() {
                return "SingleAirBean{" +
                        "AIR_LEVEL='" + AIR_LEVEL + '\'' +
                        ", CO2='" + CO2 + '\'' +
                        ", DATE_TIME='" + DATE_TIME + '\'' +
                        ", DEVICE_MAC='" + DEVICE_MAC + '\'' +
                        ", HCHO='" + HCHO + '\'' +
                        ", INSERT_TIME='" + INSERT_TIME + '\'' +
                        ", PM25='" + PM25 + '\'' +
                        ", PM25_OUT='" + PM25_OUT + '\'' +
                        ", TEMPERATURE='" + TEMPERATURE + '\'' +
                        ", TVOC='" + TVOC + '\'' +
                        ", WETNESS='" + WETNESS + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "AirQualityBean{" +
                    "data=" + data +
                    '}';
        }
    }
}
