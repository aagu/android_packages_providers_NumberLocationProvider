/*
 * Copyright (C) 2015 The SudaMod Project
 * Copyright (C) 2019 aagu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aagu.numberlocation;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NumberUtil {
    private ContentResolver mCr;
    private Context mContext;
    private Uri mUri;
    private List<String> mQueueList;
    private Map<String, NumberInfoBean> mMapCache;
    private static NumberUtil mNu;
    private static final String ERROR = "ERROR";
    private static final String AUTHORITY = "content://com.aagu.provider.NumberLocation/phonenumberlocation";
    private static final String API = "https://www.sogou.com/websearch/phoneAddress.jsp?phoneNumber=";

    public static NumberUtil getNumberUtil(Context ct) {
        if (mNu == null) {
            synchronized (NumberUtil.class) {
                if (mNu == null) {
                    mNu = new NumberUtil(ct);
                }
            }
        }
        return mNu;
    }

    private NumberUtil(Context ct) {
        this.mContext = ct.getApplicationContext();
        this.mCr = this.mContext.getContentResolver();
        this.mUri = Uri.parse("content://com.aagu.provider.NumberLocation/phonenumberlocation");
        this.mQueueList = new ArrayList();
        this.mMapCache = new ArrayMap();
        initData();
    }

    @Deprecated
    public synchronized String getLocalNumberInfo(String phoneNumber) {
        return getLocalNumberInfo(phoneNumber, true);
    }

    public synchronized String getLocalNumberInfo(String phoneNumber, boolean useMapCache) {
        phoneNumber = phoneNumber == null ? "" : phoneNumber;

        String numberFormat = phoneNumber.replaceAll("(?:-| )", "").replace("+86", "");
        NumberInfoBean infoBean = this.mMapCache.get(numberFormat);
        if ((infoBean != null) && (useMapCache)) {
            if (isNeedToUpdate(numberFormat)) {
                insertOrUpdate(numberFormat, getRequestUrl(numberFormat), true);
            }
            return infoBean.getLocation() + infoBean.getCarrier();
        }
        if (getLocalData(numberFormat)) {
            return infoBean.getLocation() + infoBean.getCarrier();
        }
        this.mQueueList.add(numberFormat);
        insertOrUpdate(numberFormat, getRequestUrl(numberFormat), false);

        return null;
    }

    public void getOnlineNumberInfo(String phoneNumber, final CallBack callBack) {
        phoneNumber = phoneNumber == null ? "" : phoneNumber;

        final String numberFormat = phoneNumber.replaceAll("(?:-| )", "").replace("+86", "");
        NumberInfoBean infoBean = this.mMapCache.get(numberFormat);
        if (infoBean != null) {
            callBack.execute(infoBean.getLocation(), infoBean.getCarrier());
            return;
        }
        if (getLocalData(numberFormat)) {
            callBack.execute(infoBean.getLocation(), infoBean.getCarrier());
            return;
        }
        NetUtil.getPhoneLocationFromNet(getRequestUrl(numberFormat), new NetUtil.NetCallBack() {
            public void execute(String response) {
                String loc = "";
                String carr = "";
                if ("ERROR".equals(response)) {
                    mQueueList.remove(numberFormat);
                }
                if (!TextUtils.isEmpty(response)) {
                    loc = responseParser(response)[0];
                    carr = responseParser(response)[1];
                    if (!mQueueList.contains(numberFormat)) {
                        mQueueList.add(numberFormat);
                        insertOrUpdateDb(numberFormat, loc, carr, false);
                    }
                }
                callBack.execute(loc, carr);
            }

            public void error() {
                callBack.execute("", "");
                mQueueList.remove(numberFormat);
            }
        });
    }

    private void insertOrUpdate(final String phoneNumber, String url, final boolean update) {
        NetUtil.getPhoneLocationFromNet(url, new NetUtil.NetCallBack() {
            public void execute(String response) {
                if ("ERROR".equals(response)) {
                    mQueueList.remove(phoneNumber);
                } else if (!TextUtils.isEmpty(response)) {
                    insertOrUpdateDb(phoneNumber, responseParser(response)[0], responseParser(response)[1], update);
                }
            }

            public void error() {
                if (update) {
                    mQueueList.remove(phoneNumber);
                }
            }
        });
    }


    private void insertOrUpdateDb(String phoneNumber, String location, String carrier, boolean update) {
        long last_time = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put("phone_number", phoneNumber);
        values.put("number_location", location);
        values.put("number_carrier", carrier);
        values.put("last_update", last_time);
        if (update) {
            this.mCr.update(this.mUri, values, "phone_number=?", new String[]{phoneNumber});
        } else {
            this.mCr.insert(this.mUri, values);
        }
        this.mMapCache.put(phoneNumber, new NumberInfoBean(phoneNumber, location, last_time, carrier));
    }

    private boolean getLocalData(String phoneNumber) {
        Cursor c = null;
        try {
            c = this.mCr.query(this.mUri, null, "phone_number=?", new String[]{phoneNumber}, null);
            boolean bool1;
            if (c.moveToFirst()) {
                this.mMapCache.put(c.getString(1), new NumberInfoBean(c.getString(1), c.getString(2), c.getLong(3), c.getString(4)));

                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void initData() {
        Cursor c = null;
        try {
            c = this.mCr.query(this.mUri, null, "last_update > " + (System.currentTimeMillis() - 604800000L), null, null);
            while (c.moveToNext()) {
                this.mMapCache.put(c.getString(1), new NumberInfoBean(c.getString(1), c.getString(2), c.getLong(3), c.getString(4)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private boolean isNeedToUpdate(String phoneNumber) {
        return (((mMapCache.get(phoneNumber)).getLastUpdateAt() + 259200000L < System.currentTimeMillis()));
    }


    private String getRequestUrl(String number) {
        StringBuilder builder = new StringBuilder();
        builder.append(API).append(number);
        return builder.toString();
    }

    private String[] responseParser(String response) {
        String str = response.split("\"")[1];
        /*location, carrier*/
        return new String[]{str.split(" ")[0], str.split(" ")[1]};
    }

    public interface CallBack {
        void execute(String loc, String carrier);
    }
}
