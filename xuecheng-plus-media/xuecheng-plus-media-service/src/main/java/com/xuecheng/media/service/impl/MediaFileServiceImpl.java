package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;
    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

    /**
     * 上传至minio
     * @param localFilePath
     * @param bucket
     * @param ObjectName
     * @param mimeType
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath, String bucket, String ObjectName, String mimeType){
        UploadObjectArgs args = null;
        try {
            args = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(ObjectName)
                    .contentType(mimeType)
                    .filename(localFilePath).build();

            minioClient.uploadObject(args);
            log.debug("上传文件成功:bucket:{}, objectName:{}", bucket, ObjectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("上传文件失败:bucket:{}, objectName:{}, error:{}", bucket, ObjectName, e);
        }
        return false;
    }

    /**
     * 根据后缀得到mimeType
     * @param extension
     * @return
     */
    private String getMimeType(String extension){
        if(extension == null){
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);

        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 得到文件的MD5
     * @param file
     * @return
     */
    private String getFileMd5(File file){
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            return md5Hex;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件的默认目录 年/月/日
     * @return
     */
    private String getDefaultFolderPath(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * 将文件信息添加至数据库
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param bucket
     * @param objectName
     * @return
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,
                                        String fileMd5,
                                        UploadFileParamsDto uploadFileParamsDto,
                                        String bucket,
                                        String objectName){
        //查询文件 是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());

            // 记录待处理任务 和保存文件信息到文件表一起进行事务控制
            // 通过mimeType判断如果是avi视频 写入待处理任务
            // 向MediaProcess插入记录
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    /**
     * 添加待处理任务
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        // 获取文件的mimeType
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        //如果是avi视频写入待处理任务表
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            //状态是未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }

    }


    /**
     * 检查文件
     * @param fileMd5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //若数据库存在，再查询minio
        if(mediaFiles != null){
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs args = GetObjectArgs.builder()
                            .bucket(bucket)
                                    .object(filePath)
                                            .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(args);
                if(inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5){
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }
    private String getFilePathByMd5(String fileMd5, String fileExt){
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 +fileExt;
    }

    /**
     * 检查分块
     * @param fileMd5
     * @param chunkIndex
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //分块存储路径：md5前两位为2个目录，chunk存储分块文件
        //根据md5得到分块文件所在目录的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        GetObjectArgs args = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();

        try {
            FilterInputStream inputStream = minioClient.getObject(args);
            if(inputStream != null){
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    /**
     * 上传分块
     * @param fileMd5
     * @param chunk
     * @param localChunkFilePath
     * @return
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String mimeType = getMimeType(null);
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        //将分块文件上传至minio
        boolean b = addMediaFilesToMinIO(localChunkFilePath, bucket_video, chunkFilePath, mimeType);
        if(!b){
            return RestResponse.validfail(false, "上传分块文件失败");
        }

        return RestResponse.success(true);
    }

    /**
     * 合并分块
     * @param companyId
     * @param fileMd5
     * @param chunkTotal
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    @Override
    public RestResponse mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

        //分块文件所在的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        //===========找到分块文件调用minio的sdk进行文件合并=========
        List<ComposeSource> composeSourceList = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build())
                .collect(Collectors.toList());

        //源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        //合并后的文件名
        String objectName = getFilePathByMd5(fileMd5, extension);
        ComposeObjectArgs args = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)//合并后的文件name
                .sources(composeSourceList)
                .build();
        try {
            minioClient.composeObject(args);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}",bucket_video, objectName, e);
        }

        //============检验minio合并后的和源文件的是否一致============
        //先把minio的文件下载下来
        File file = downloadFileFromMinIO(bucket_video, objectName);
        String mergeFileMd5 = getFileMd5(file);
        if(!fileMd5.equals(mergeFileMd5)){
            log.error("检验合并文件md5值不一致，原始文件:{}, 合并文件:{}", fileMd5, mergeFileMd5);
            return RestResponse.validfail(false, "文件校验失败");
        }
        uploadFileParamsDto.setFileSize(file.length());

        //==================将文件信息入库=============
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles == null){
            return RestResponse.validfail(false, "文件入库失败");
        }

        //================清理分块文件=========
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 上传文件
     * @param companyId
     * @param uploadFileParamsDto
     * @param localFilePath 本地磁盘路径
     * @return
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName) {
        File file = new File(localFilePath);
        if(!file.exists()){
            XueChengPlusException.cast("文件不存在");
        }

        String filename = uploadFileParamsDto.getFilename();
        //object 路径+md5值+格式后缀
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //根据后缀得到mimeType
        String mimeType = getMimeType(extension);
        //md5
        String fileMd5 = getFileMd5(file);
        //默认目录 年/月/日
        String defaultFolderPath = getDefaultFolderPath();
        //存储到minIO中的对象名(带目录)
        if(StringUtils.isEmpty(objectName)){
            //使用默认的年月日去存储
            objectName = defaultFolderPath + fileMd5 + extension;
        }

        //将文件上传至minio
        boolean b = addMediaFilesToMinIO(localFilePath, bucket_mediafiles, objectName, mimeType);

        //文件大小
        uploadFileParamsDto.setFileSize(file.length());
        //将文件信息存储到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);

        //准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    /**
     * 从minio下载文件
     * @param bucket
     * @param objectName
     * @return
     */
    public File downloadFileFromMinIO(String bucket,String objectName) {
        //在本地创建临时文件 通过流拷贝 下载到本地
        File minioFile = null;
        OutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());

            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath
     * @param chunkTotal
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal){
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath + i))
                .collect(Collectors.toList());

        RemoveObjectsArgs args = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();

        minioClient.removeObjects(args);
    }

}
