package com.hzgc.service.people.controller;

import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.service.people.service.ParamPickService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Api(value = "/people", tags = "人口库服务")
public class ParamPickController {
    @Autowired
    private ParamPickService paramPickService;


    public ResponseResult<Map<Integer, String>> getEdulevelPick() {
        Map<Integer, String> map =  paramPickService.getEdulevelPick();
        return ResponseResult.init(map);
    }

    public ResponseResult<Map<Integer, String>> getFlagPick() {
        Map<Integer, String> map =  paramPickService.getFlagPick();
        return ResponseResult.init(map);
    }

    public ResponseResult<Map<Integer, String>> getPoliticPick() {
        Map<Integer, String> map =  paramPickService.getPoliticPick();
        return ResponseResult.init(map);
    }

    public ResponseResult<Map<Integer, String>> getProvincesPick() {
        Map<Integer, String> map =  paramPickService.getProvincesPick();
        return ResponseResult.init(map);
    }

    public ResponseResult<Map<Integer, String>> getCityPick(int index) {
        Map<Integer, String> map =  paramPickService.getCityPick(index);
        return ResponseResult.init(map);
    }
}
