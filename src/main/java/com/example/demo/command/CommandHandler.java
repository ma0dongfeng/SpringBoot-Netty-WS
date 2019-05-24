package com.example.demo.command;

import com.alibaba.fastjson.JSONObject;

public interface CommandHandler {
    void handle(JSONObject recJson) throws Exception;
}
