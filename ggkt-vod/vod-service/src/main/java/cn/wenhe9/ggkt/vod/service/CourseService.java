package cn.wenhe9.ggkt.vod.service;

import cn.wenhe9.ggkt.vod.entity.Course;
import cn.wenhe9.ggkt.vod.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程 服务类
 * </p>
 *
 * @author DuJinliang
 * @since 2022-08-18
 */
public interface CourseService extends IService<Course> {

    /**
     * 点播课程列表
     */
    Map<String , Object> findPageCourse(long current, long limit, CourseQueryVo courseQueryVo);

    /**
     * 添加课程基本信息
     */
    Long saveCourseInfo(CourseFormVo courseFormVo);

    /**
     * 根据id获取课程信息
     */
    CourseFormVo getCourseInfoById(long id);

    /**
     * 修改课程信息
     */
    void updateCourseById(CourseFormVo courseFormVo);

    /**
     * 根据课程id查询发布课程信息
     */
    CoursePublishVo getCoursePublishVo(long id);

    /**
     * 课程最终发布
     */
    void publishCourse(long id);

    /**
     * 根据id删除课程
     */
    void removeCourseById(long id);

    /**
     * 根据关键字查询课程
     */
    List<Course> findCourseByKeyword(String keyword);

    /**
     * 根据课程分类查询课程列表
     */
    Page<CourseVo> findCourseByCategory(long subjectParentId, long current, long limit);

    /**
     * 根据课程id查询课程详情
     */
    Map<String, Object> findCourseInfoByCourseId(long courseId);
}
