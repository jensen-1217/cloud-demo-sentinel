package cn.itcast.order.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * @author jensen
 * @date 2024-10-07 13:24
 * @description
 */
@Component
public class HelloJob {
    @XxlJob("helloJob")
    public ReturnT helloJob(String params){
        System.out.println("定时任务触发：查询待支付订单");
        return ReturnT.SUCCESS;
    }
}
