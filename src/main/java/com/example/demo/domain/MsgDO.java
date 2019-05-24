package com.example.demo.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MsgDO {
    String msg;
    String msgTime;
    String msgType;
    Long msgId;
    Long talkId;
    Long cid;//客户端id
}
