package com.example.demo.command.commands;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.command.CommandHandler;
import com.example.demo.dao.MsgDao;
import com.example.demo.domain.MsgDO;
import org.springframework.beans.factory.annotation.Autowired;

public class CUPHandler implements CommandHandler {
    @Autowired
    MsgDao msgDao;
    @Override
    public void handle(JSONObject recJson) throws Exception {
        String msgTime = recJson.getString("msg_time");
        String msg = recJson.getString("msg");
        MsgDO msgDO = new MsgDO();
        msgDO.setCid(111L);
        msgDO.setMsg(msg);
        msgDO.setMsgId(1111L);
        msgDO.setMsgTime(msgTime);
        msgDO.setMsgType("c");
        msgDO.setTalkId(11111L);
        msgDao.save(msgDO);
    }
}
