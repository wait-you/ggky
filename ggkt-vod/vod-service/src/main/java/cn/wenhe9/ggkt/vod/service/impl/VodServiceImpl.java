package cn.wenhe9.ggkt.vod.service.impl;

import cn.wenhe9.ggkt.common.exception.GgktException;
import cn.wenhe9.ggkt.common.result.ResultResponseEnum;
import cn.wenhe9.ggkt.vod.utils.ConstantVodProperties;
import cn.wenhe9.ggkt.vod.service.VodService;
import cn.wenhe9.ggkt.vod.utils.Signature;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.DeleteMediaRequest;
import com.tencentcloudapi.vod.v20180717.models.DeleteMediaResponse;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author DuJinliang
 * 2022/08/20
 */
@Service
public class VodServiceImpl implements VodService {

    @Override
    public void removeVideoById(String id) {
        try {
            Credential credential = new Credential(ConstantVodProperties.ACCESS_KEY, ConstantVodProperties.SECRET_KEY);

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            VodClient vodClient = new VodClient(credential, ConstantVodProperties.REGION, clientProfile);

            DeleteMediaRequest request = new DeleteMediaRequest();
            request.setFileId(id);

            vodClient.DeleteMedia(request);
        }catch (Exception e) {
            throw new GgktException(ResultResponseEnum.FILE_DELETE_ERROR);
        }
    }

    @Override
    public String getSign() {
        Signature sign = new Signature();
        // 设置 App 的云 API 密钥
        sign.setSecretId(ConstantVodProperties.ACCESS_KEY);
        sign.setSecretKey(ConstantVodProperties.SECRET_KEY);
        sign.setCurrentTime(System.currentTimeMillis() / 1000);
        sign.setRandom(new Random().nextInt(java.lang.Integer.MAX_VALUE));
        // 签名有效期：2天
        sign.setSignValidDuration(3600 * 24 * 2);
        try {
            return sign.getUploadSignature();
        } catch (Exception e) {
            throw new GgktException(ResultResponseEnum.SIGN_GET_ERROR);
        }
    }
}
