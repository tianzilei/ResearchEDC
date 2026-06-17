package org.researchedc.config;

import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.regex.Pattern;

@Configuration
public class DaoRegistrar implements BeanDefinitionRegistryPostProcessor {

    private static final Pattern DAO_NAME_PATTERN = Pattern.compile(".*DAO$|.*Dao$");
    private static final String[] SKIP_CLASSES = {
        "DatabaseChangeLogDao",
        "ItemFormMetadataDAO", "SectionDAO", "FilterDAO", "StudyGroupClassDAO", "StudyGroupDAO",
        "RuleDAO", "RuleSetDAO", "DiscrepancyNoteDAO",                 "StudyParameterValueDao", "RuleActionRunLogDao", "RuleSetAuditDao", "SCDItemMetadataDao",
        "DynamicsItemGroupMetadataDao", "DynamicsItemFormMetadataDao",
    };

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(DAO_NAME_PATTERN));

        for (var bd : scanner.findCandidateComponents("org.researchedc.dao")) {
            String className = bd.getBeanClassName();
            if (className == null || shouldSkip(className)) continue;

            String simpleName = className.substring(className.lastIndexOf('.') + 1);
            String beanName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);

            if (beanName.equals("cRFDAO") || beanName.equals("cRFVersionDAO")) {
                beanName = simpleName;
            }

            if (registry.containsBeanDefinition(beanName)) continue;
            if (isAlreadyManaged(registry, simpleName)) continue;

            Class<?> clazz = loadClass(className);
            if (clazz == null) continue;

            BeanDefinitionBuilder builder = BeanDefinitionBuilder
                    .genericBeanDefinition(className)
                    .setScope("singleton");

            boolean injected = false;

            for (var ctor : clazz.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();

                if (params.length == 0) {
                    break;
                }

                if (params.length == 1 && params[0] == DataSource.class) {
                    builder.addConstructorArgReference("dataSource");
                    injected = true;
                    break;
                }

                if (params.length >= 2 && params[0] == DataSource.class) {
                    builder.addConstructorArgReference("dataSource");
                    injected = true;
                    break;
                }
            }

            if (!injected && clazz.getConstructors().length > 0) {
                continue;
            }

            registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    private boolean shouldSkip(String className) {
        for (String skip : SKIP_CLASSES) {
            if (className.endsWith("." + skip)) return true;
        }
        return className.contains(".spi.") || className.contains(".core.");
    }

    private boolean isAlreadyManaged(BeanDefinitionRegistry registry, String simpleName) {
        String altName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        return registry.containsBeanDefinition(simpleName) || registry.containsBeanDefinition(altName);
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
