package swyp.swyp6_team7.global.config;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import swyp.swyp6_team7.global.utils.auth.RequireUserNumber;
import swyp.swyp6_team7.global.utils.auth.MemberAuthorizeUtil;

@Component
public class UserNumberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @RequireUserNumber 어노테이션이 붙고, 타입이 UserInfo인 경우 처리
        return parameter.hasParameterAnnotation(RequireUserNumber.class)
               && parameter.getParameterType().equals(Integer.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return MemberAuthorizeUtil.getLoginUserNumber();// 인증되지 않은 경우 null 반환
    }
}
