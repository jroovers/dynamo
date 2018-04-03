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
package com.ocs.dynamo.domain.model.impl;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.GraphEntityModel;

/**
 * A wrapper that adds lazy loading to an entity model definition for graphs
 * 
 * @author patrickdeenen
 *
 */
public class LazyGraphEntityModelWrapper<T> extends LazyEntityModelWrapper<T> implements GraphEntityModel<T> {

	public LazyGraphEntityModelWrapper(EntityModelFactory factory, String reference, Class<T> entityClass) {
		super(factory, reference, entityClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ocs.dynamo.domain.model.impl.LazyEntityModelWrapper#getDelegate()
	 */
	@Override
	protected GraphEntityModel<T> getDelegate() {
		return (GraphEntityModel<T>) super.getDelegate();
	}

	@Override
	public String getSubTitle() {
		return getDelegate().getSubTitle();
	}

	@Override
	public String getTooltip() {
		return getDelegate().getTooltip();
	}

	@Override
	public AttributeModel getSeriesAttributeModel() {
		return getDelegate().getSeriesAttributeModel();
	}

	@Override
	public AttributeModel getNameAttributeModel() {
		return getDelegate().getNameAttributeModel();
	}

	@Override
	public AttributeModel getDataAttributeModel() {
		return getDelegate().getDataAttributeModel();
	}

}
