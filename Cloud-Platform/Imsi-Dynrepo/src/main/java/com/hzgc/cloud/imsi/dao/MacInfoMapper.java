package com.hzgc.cloud.imsi.dao;

import com.hzgc.cloud.imsi.model.MacInfo;
import com.hzgc.cloud.imsi.model.MacParam;
import com.hzgc.cloud.imsi.model.SearchMacDTO;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@CacheNamespace
public interface MacInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MacInfo record);

    int insertSelective(MacInfo record);

    List <MacInfo> selectBySns(MacParam macParam);

    MacInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MacInfo record);

    int updateByPrimaryKey(MacInfo record);

    List<MacInfo> searchMac(SearchMacDTO searchMacDTO);
}