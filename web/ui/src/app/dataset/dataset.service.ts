/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Injectable} from "@angular/core";
import {Http, Response} from "@angular/http";
import "rxjs";
import {IDataLineage, IPersistedDatasetDescriptor} from "../../generated-ts/lineage-model";

@Injectable()
export class DatasetService {
    private datasetPromiseCache: { [id: string]: Promise<IPersistedDatasetDescriptor>; } = {}
    private overviewPromiseCache: { [id: string]: Promise<IDataLineage>; } = {}

    constructor(private http: Http) {
    }

    getDatasetDescriptor(id: string): Promise<IPersistedDatasetDescriptor> {
        let fetchAndCache = (id: string) => {
            let dsp = this.http.get(`rest/dataset/${id}/descriptor`).map((res: Response) => res.json()).toPromise()
            this.datasetPromiseCache[id] = dsp
            return dsp
        }

        let cachedPromise = this.datasetPromiseCache[id]
        return (cachedPromise) ? cachedPromise : fetchAndCache(id)
    }

    getLineageOverview(datasetId: string): Promise<IDataLineage> {
        let fetchAndCache = (id: string) => {
            let lop = this.http.get(`rest/dataset/${id}/lineage/overview`).map((res: Response) => res.json()).toPromise()
            this.overviewPromiseCache[id] = lop
            return lop.then(expandCacheForAllRelatedDatasets)
        }

        let expandCacheForAllRelatedDatasets = (lineage: IDataLineage) => {
            lineage.datasets.forEach(ds =>
                this.overviewPromiseCache[ds.id] = Promise.resolve(lineage))
            return lineage
        }

        let cachedPromise = this.overviewPromiseCache[datasetId]
        return (cachedPromise) ? cachedPromise : fetchAndCache(datasetId)
    }
}