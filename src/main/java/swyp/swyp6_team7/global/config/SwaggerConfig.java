package swyp.swyp6_team7.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Moing API")
                .description("여행 커뮤니티 서비스 '모잉' 백엔드 API")
                .version("1.0.0");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }

}
