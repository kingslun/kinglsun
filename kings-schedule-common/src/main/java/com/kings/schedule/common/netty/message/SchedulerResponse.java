package com.kings.schedule.common.netty.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author lun.kings
 * @date 2020/7/30 4:13 下午
 * @email lun.kings@zatech.com
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class SchedulerResponse implements Serializable {
    private String code;
    private String msg;
}
