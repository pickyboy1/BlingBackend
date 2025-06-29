package com.pickyboy.yuquebackend.controller;

import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.common.utils.MinioUtil;
import com.pickyboy.yuquebackend.domain.vo.upload.UploadURLResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    @Autowired
    private MinioUtil minioUtil;

    @PostMapping("/request-url")
    public Result<UploadURLResponseVO> requestUploadUrl(@RequestBody UploadURLRequestVO requestVO) {
        // 参数校验 (此处省略)
        // ...
/*        Long UserId = CurrentHolder.getCurrentUserId();
        if(UserId==null){
            return Result.error("请重新登录");
        }*/
        UploadURLResponseVO response = minioUtil.generatePresignedUploadUrl(
                requestVO.getFileName(),
                requestVO.getUploadType(),
                "test"
        );

        return Result.success(response);
    }
    
    // 请求的VO类定义
    @lombok.Data
    public static class UploadURLRequestVO {
        private String fileName;
        private String fileType;
        private Long fileSize;
        private String uploadType;
    }
}