package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: MediaFileProcesServiceImpl
 * Package: com.xuecheng.media.service.impl
 * Description:
 *
 * @Author zbc
 * @Create 2024/3/19 14:23
 * @Version 1.0
 */
@Service
@Slf4j
public class MediaFileProcesServiceImpl implements MediaFileProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;
    @Override
    public List<MediaProcess> selectListByShardIndex(int shardTotal, int shardIndex, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <= 0? false:true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            log.debug("更新该任务状态时，此任务:{}，为null", taskId);
            return;
        }
        //###如果任务执行失败
        LambdaUpdateWrapper<MediaProcess> wrapper = new LambdaUpdateWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);;
        if(status.equals("3")){
            wrapper.set(MediaProcess::getStatus, "3")
                    .set(MediaProcess::getFailCount, mediaProcess.getFailCount() + 1)
                    .set(MediaProcess::getErrormsg, errorMsg);
            mediaProcessMapper.update(null, wrapper);
            return;
        }

        //###如果任务执行成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        //1. 更新文件表中的url
        if(mediaFiles != null){
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        //2. 更新任务表的状态 将任务表中记录插入到任务历史表中
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);

        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //3. 从任务表中删除当前任务
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }

    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }
}
