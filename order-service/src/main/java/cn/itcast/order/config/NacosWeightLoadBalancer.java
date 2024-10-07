package cn.itcast.order.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.*;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于nacos权重的负载均衡
*/
public class NacosWeightLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Log log = LogFactory.getLog(NacosWeightLoadBalancer.class);
    private final String serviceId;
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    //nacos权重获取名称，在nacos元数据中
    private static final String NACOS_WEIGHT_NAME = "nacos.weight";

    public NacosWeightLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map((serviceInstances) -> {
            return this.processInstanceResponse(supplier, serviceInstances);
        });
    }


    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback)supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }

        return serviceInstanceResponse;
    }


    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + this.serviceId);
            }
        } else {
            //根据权重选择实例，权重高的被选中的概率大
            //nacos.weight的值越大，被选中的概率越大
            Double totalWeight = 0D;
            for (ServiceInstance instance : instances) {
                String s = instance.getMetadata().get(NACOS_WEIGHT_NAME);
                double weight = Double.parseDouble(s);
                totalWeight = totalWeight + weight;
                //放置当前实例的权重区间
                instance.getMetadata().put("weight",String.valueOf(totalWeight));
            }
            //随机获取一个区间类的数值，nacos权重越大，区间越大，则随机数值落到相应的区间的概率是由区间的大小来决定的。
            double index = ThreadLocalRandom.current().nextDouble(totalWeight);
            //根据权重区间选择实例
            for (ServiceInstance instance : instances) {
                double weight = Double.parseDouble(instance.getMetadata().get("weight"));
                if (index <= weight) {
                    return new DefaultResponse(instance);
                }
            }

        }
        return new EmptyResponse();
    }
}
