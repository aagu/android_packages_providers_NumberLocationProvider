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

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetUtil
{   private static final String TAG = "NetUtil";

    public static void getPhoneLocationFromNet(String url, final NetCallBack callback)
    {
        OkHttpClient mOkHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            public void onFailure(Call call, IOException e)
            {
                callback.error();
            }

            public void onResponse(Call call, Response response)
                    throws IOException
            {
                callback.execute(response.body().string());
            }
        });
    }

    public static Response getPhoneLocationFromNet(String url)
    {
        OkHttpClient mOkHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        Call call = mOkHttpClient.newCall(request);
        try
        {
            return call.execute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public interface NetCallBack
    {
        void execute(String paramString);

        void error();
    }
}
