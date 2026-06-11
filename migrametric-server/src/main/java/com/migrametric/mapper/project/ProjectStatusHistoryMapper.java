package com.migrametric.mapper.project;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.project.ProjectStatusHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目状态历史 Mapper
 */
@Mapper
public interface ProjectStatusHistoryMapper extends BaseMapper<ProjectStatusHistory> {
}
