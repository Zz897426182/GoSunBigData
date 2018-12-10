package com.hzgc.system.dao;

import com.hzgc.system.domain.MachineDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * 物理机器
 * @author liang
 * @email 1992lcg@163.com
 * @date 2018-12-07 18:35:53
 */
@Mapper
public interface MachineDao {

	MachineDO get(Integer machineId);
	
	List<MachineDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(MachineDO machine);
	
	int update(MachineDO machine);
	
	int remove(Integer machine_id);
	
	int batchRemove(Integer[] machineIds);
}
