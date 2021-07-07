/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.api.cache;

import java.io.Serializable;
import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ApiCacheKey implements Serializable, Comparable<ApiCacheKey>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private static final Comparator<ApiCacheKey> COMPARATOR =
            Comparator.comparing(ApiCacheKey::getUrl).thenComparing(ApiCacheKey::getUserId);
	
	@Getter
	@Setter
	public String url;

	@Getter
	@Setter
	public String idApi;
	
	@Getter
	@Setter
	public String userId;
	
    @Override
    public int hashCode() {
        return (url.hashCode() * 31 + userId.hashCode()) * 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ApiCacheKey)) {
            return false;
        }
        ApiCacheKey other = (ApiCacheKey) obj;
        return userId.equals(other.userId) && url.equals(other.url);
    }

	@Override
	public int compareTo(ApiCacheKey o) {
		return COMPARATOR.compare(this, o);
	}

}
