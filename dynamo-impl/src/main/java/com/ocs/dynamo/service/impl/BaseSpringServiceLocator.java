/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.service.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;

/**
 * Static class for accessing the Spring container
 * 
 * @author bas.rutten
 */
public abstract class BaseSpringServiceLocator implements ServiceLocator {

	protected ApplicationContext ctx;

	protected abstract void loadCtx();

	/**
	 * @return
	 */
	private ApplicationContext getContext() {
		if (ctx == null) {
			loadCtx();
		}
		return ctx;
	}

	/**
	 * Retrieves a service of a certain type
	 * 
	 * @param clazz
	 *            the class of the service
	 * @return
	 */
	public <T> T getService(Class<T> clazz) {
		return getContext().getBean(clazz);
	}

	/**
	 * Retrieves the message service from the context
	 * 
	 * @return
	 */
	public MessageService getMessageService() {
		return getService(MessageService.class);
	}

	/**
	 * Retrieves the entity model factory from the context
	 * 
	 * @return
	 */
	public EntityModelFactory getEntityModelFactory() {
		return getService(EntityModelFactory.class);
	}

	/**
	 * Returns a service that is used to manage a certain type of entity
	 * 
	 * @param entityClass
	 *            the entity class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public BaseService<?, ?> getServiceForEntity(Class<?> entityClass) {
		Map<String, BaseService> services = getContext().getBeansOfType(BaseService.class, false, true);
		for (Entry<String, BaseService> e : services.entrySet()) {
			if (e.getValue().getEntityClass() != null && e.getValue().getEntityClass().equals(entityClass)) {
				return (BaseService<?, ?>) e.getValue();
			}
		}
		return null;
	}

}
