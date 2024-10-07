package cn.itcast.feign.fallback;

import cn.itcast.feign.clients.UserClient;
import cn.itcast.feign.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author jensen
 * @date 2024-10-07 11:49
 * @description
 */
@Slf4j
public class UserClientFallback  implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public User findById(Long id, String info) {
                log.error("查询用户异常", cause);
                return new User();
            }
        };
    }
}
