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
package com.ocs.dynamo.functional.domain.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.functional.domain.Translation;
import com.ocs.dynamo.functional.domain.TranslationService;
import com.ocs.dynamo.service.impl.BaseServiceImpl;

/**
 * @author Patrick.Deenen@OpenCircleSolutions.nl
 * 
 *         The service for translations
 *
 */
public class TranslationServiceImpl extends BaseServiceImpl<Integer, Translation<?>> implements TranslationService {

	@Autowired
	private BaseDao<Integer, Translation<?>> translationDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> fetchByIds(Class<?> entity, String field, String locale, List<Integer> ids) {
		if (ids.isEmpty()) {
			return new ArrayList<>();
		}
		Filter filter = new And(//
				new Compare.Equal("type", entity.getSimpleName()), //
				new Compare.Equal("field", field), //
				new Like("locale.code", locale, false), //
				new In("key", ids));
		SortOrders so = new SortOrders(new SortOrder("translation"));
		List<Object[]> unsortedTranslations = (List<Object[]>) fetchSelect(filter, new String[]{"key", "translation"}, so);
		List<Object[]> sortedTranslations = new ArrayList<>();
		for (Integer id : ids) {
			for (Object[] translation : unsortedTranslations) {
				Integer translationId = (Integer)translation[0];
				if (translationId.equals(id)) {
					sortedTranslations.add(translation);
				}
			}
		}
		return sortedTranslations;
	}

	@Override
	protected BaseDao<Integer, Translation<?>> getDao() {
		return translationDao;
	}

}
