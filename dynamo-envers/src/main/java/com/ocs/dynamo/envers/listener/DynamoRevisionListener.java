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
package com.ocs.dynamo.envers.listener;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.envers.RevisionListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ocs.dynamo.envers.domain.DynamoRevisionEntity;

/**
 * Custom Hibernate envers listener
 * 
 * @author bas.rutten
 *
 */
public class DynamoRevisionListener implements RevisionListener {

	private static final String UNKNOWN = "unknown";

	@Override
	public void newRevision(final Object o) {
		final DynamoRevisionEntity entity = (DynamoRevisionEntity) o;
		try {
			final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();
			if (request != null && request.getUserPrincipal() != null) {
				entity.setUsername(request.getUserPrincipal().getName());
			} else {
				entity.setUsername(UNKNOWN);
			}
		} catch (IllegalStateException ex) {
			// in case no request present
			entity.setUsername(UNKNOWN);
		}
	}
}
