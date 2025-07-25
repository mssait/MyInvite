package com.hionstudios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;

public class MixMultipartFileAndStringArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(MixMultipartFileAndString.class) != null;
    }

    /**
     *
     * @param methodParameter the method parameter to resolve. This parameter must
     *                        have previously been passed to
     *                        {@link #supportsParameter} which must
     *                        have returned {@code true}.
     * @param mavContainer    the ModelAndViewContainer for the current request
     * @param request         the current request
     * @param binderFactory   a factory for creating {@link WebDataBinder} instances
     * @return
     * @throws Exception
     * @see org.springframework.web.method.annotation.RequestParamMethodArgumentResolver#resolveName(String,
     *      MethodParameter, NativeWebRequest)
     */
    @Override
    public Object resolveArgument(@NonNull MethodParameter methodParameter,
            @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest request,
            @Nullable WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        String name = methodParameter.getParameterName();
        if (servletRequest != null && name != null) {
            Object mpArg = MultipartResolutionDelegate.resolveMultipartArgument(name, methodParameter, servletRequest);
            if (mpArg != MultipartResolutionDelegate.UNRESOLVABLE) {
                return mpArg;
            }
        }

        List<Object> arg = new ArrayList<>();
        MultipartRequest multipartRequest = request.getNativeRequest(MultipartRequest.class);
        if (name != null) {
            if (multipartRequest != null) {
                MultipartFile file = multipartRequest.getFile(name);
                if (file != null) {
                    arg.add(file);
                }
            }

            String[] paramValues = request.getParameterValues(name);
            if (paramValues != null) {
                Collections.addAll(arg, paramValues);
            }
        }

        return arg;
    }
}
