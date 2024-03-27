package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {
 /**
  * 根据id查文件信息
  * @param mediaId
  * @return
  */
 MediaFiles getFileById(String mediaId);

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件通用方法
  * @param companyId
  * @param uploadFileParamsDto
  * @param localFilePath 本地磁盘路径
  * @return
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

 /**
  * 事务优化
  * @param companyId
  * @param fileMd5
  * @param uploadFileParamsDto
  * @param bucket_mediafiles
  * @param objectName
  * @return
  */
 MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket_mediafiles, String objectName);

 /**
  *检查文件是否存在
  * @param fileMd5
  * @return
  */
 public RestResponse<Boolean> checkFile(String fileMd5);

 /**
  * 检查分块是否存在
  * @param fileMd5
  * @param chunkIndex
  * @return
  */
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 /**
  * 上传分块
  * @param fileMd5
  * @param chunk
  * @param localChunkFilePath
  * @return
  */
 public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

 /**
  * 合并分块
  * @param companyId
  * @param fileMd5
  * @param chunkTotal
  * @param uploadFileParamsDto 文件信息
  * @return
  */
 public RestResponse mergeChunk(Long companyId,String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

 /**
  * 从minio下载文件
  * @param bucket
  * @param objectName
  * @return
  */
 public File downloadFileFromMinIO(String bucket, String objectName);

 /**
  * 上传文件至minio
  * @param localFilePath
  * @param bucket
  * @param ObjectName
  * @param mimeType
  * @return
  */
 public boolean addMediaFilesToMinIO(String localFilePath, String bucket, String ObjectName, String mimeType);
}
