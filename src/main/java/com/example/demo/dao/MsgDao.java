package com.example.demo.dao;

import com.example.demo.domain.MsgDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MsgDao {
    MsgDO get(Long talkId);

    int save(MsgDO msg);

}
