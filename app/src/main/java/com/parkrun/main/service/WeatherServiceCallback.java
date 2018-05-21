package com.parkrun.main.service;

import com.parkrun.main.objects.Channel;

public interface WeatherServiceCallback
{
    void serviceSuccess(Channel channel);

    void serviceFailure(Exception exception);
}
