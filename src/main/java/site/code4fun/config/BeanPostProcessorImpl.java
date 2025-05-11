package site.code4fun.config;

import lombok.NonNull;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

//@Component
@SuppressWarnings("all")
public class BeanPostProcessorImpl implements BeanPostProcessor, Ordered {
	@Override
	public Object postProcessBeforeInitialization(@NonNull Object bean,@NonNull String beanName) throws BeansException {
		LogFactory.getLog(this.getClass()).info("Before Processing- " + beanName);
		return bean;
	}
	@Override
	public Object postProcessAfterInitialization(@NonNull Object bean,@NonNull String beanName) throws BeansException {
		LogFactory.getLog(this.getClass()).info("After Processing- " + beanName);
		return bean;
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}
}