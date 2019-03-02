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

public class NumberInfoBean
{
    private String number;
    private String location;
    private String carrier;
    private long lastUpdateAt;

    public NumberInfoBean(String number, String loc, long lastUpdateAt, String carrier)
    {
        this.number = number;
        this.location = loc;
        this.lastUpdateAt = lastUpdateAt;
        this.carrier = carrier;
    }

    public String getNumber()
    {
        return this.number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getLocation()
    {
        return this.location;
    }

    public void setLocation(String loc)
    {
        this.location = loc;
    }

    public String getCarrier()
    {
        return this.carrier;
    }

    public void setCarrier(String carrier)
    {
        this.carrier = carrier;
    }

    public long getLastUpdateAt()
    {
        return this.lastUpdateAt;
    }

    public void setLastUpdateAt(long lastUpdateAt)
    {
        this.lastUpdateAt = lastUpdateAt;
    }
}
