package cn.wenhe9.ggkt.user.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BindPhoneVo {
	
	@ApiModelProperty(value = "手机号")
	private String phone;

	@ApiModelProperty(value = "验证码")
	private String code;

}

