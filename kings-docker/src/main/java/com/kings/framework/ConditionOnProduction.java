package com.kings.framework;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * <p>
 * 再生产环境下加载spring bean
 * env/ENV: pro/prod
 * </p>
 *
 * @author lun.wang
 * @date 2021/6/24 2:08 下午
 * @since v1.0
 */
@Conditional(ConditionOnProduction.ProductionCondition.class)
public @interface ConditionOnProduction {
    class ProductionCondition implements Condition {
        @Override
        public boolean matches(ConditionContext conditionContext,
                               @NotNull AnnotatedTypeMetadata annotatedTypeMetadata) {
            final Environment environment = conditionContext.getEnvironment();
            return production(environment.getProperty("ENV")) || production(environment.getProperty("env"));
        }

        private boolean production(String env) {
            return StringUtils.hasText(env) && (Objects.equals(env, "pro") || Objects.equals(env, "prod"));
        }
    }
}