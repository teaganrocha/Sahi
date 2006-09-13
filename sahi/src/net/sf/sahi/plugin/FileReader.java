/**
 * Copyright V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.sahi.plugin;

import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.request.HttpRequest;

public class FileReader {
    public HttpResponse contents(HttpRequest request){
        return new HttpFileResponse(request.getParameter("fileName"));
    }
}