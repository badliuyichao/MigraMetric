package com.migrametric.mapper.project;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.migrametric.entity.project.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目Mapper接口
 *
 * @author MigraMetric Team
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
