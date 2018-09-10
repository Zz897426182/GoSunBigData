package com.hzgc.service.people.dao;

import com.hzgc.service.people.model.People;
import com.hzgc.service.people.param.FilterField;

import java.util.List;

public interface PeopleMapper {
    int deleteByPrimaryKey(String id);

    int insert(People record);

    int insertSelective(People record);

    People selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(People record);

    int updateByPrimaryKey(People record);

    List<People> searchPeople(FilterField field);
}