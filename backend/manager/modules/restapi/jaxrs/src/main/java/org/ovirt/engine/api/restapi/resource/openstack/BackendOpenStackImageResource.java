/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.resource.openstack.OpenStackImageResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackImageResource
        extends AbstractBackendActionableResource<OpenStackImage, RepoImage>
        implements OpenStackImageResource {
    private String providerId;

    protected BackendOpenStackImageResource(String providerId, String id) {
        super(id, OpenStackImage.class, RepoImage.class);
        this.providerId = providerId;
    }

    @Override
    public OpenStackImage get() {
        Guid storageDomainId = BackendOpenStackImageProviderHelper.getStorageDomainId(this, providerId);
        return performGet(VdcQueryType.GetImageById, new GetImageByIdParameters(storageDomainId, id));
    }

    @Override
    protected OpenStackImage doPopulate(OpenStackImage model, RepoImage entity) {
        return model;
    }

    @Override
    protected OpenStackImage addParents(OpenStackImage image) {
        OpenStackImageProvider provider = new OpenStackImageProvider();
        provider.setId(providerId);
        image.setOpenstackImageProvider(provider);
        return super.addParents(image);
    }

    @Override
    public Response doImport(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = BackendOpenStackImageProviderHelper.getStorageDomainId(this, providerId);
        ImportRepoImageParameters parameters = new ImportRepoImageParameters();
        parameters.setSourceRepoImageId(id);
        parameters.setSourceStorageDomainId(storageDomainId);
        parameters.setStoragePoolId(getDataCenterId(getStorageDomainId(action)));
        parameters.setStorageDomainId(getStorageDomainId(action));
        if (action.isSetImportAsTemplate()) {
            if (action.isImportAsTemplate()) {
                validateParameters(action, "cluster.id|name");
                parameters.setClusterId(getClusterId(action));
            }
            parameters.setImportAsTemplate(action.isImportAsTemplate());
        }
        return doAction(VdcActionType.ImportRepoImage, parameters, action);
    }
}
