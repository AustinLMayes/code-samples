/*
The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned
project and will in most cases not function out of the box. This file is merely intended as a representation of the
design pasterns and different problem-solving approaches I use to tackle various problems.

The original file can be found here: N/A (Private Codebase)
*/

package network.walrus.infrastructure.objects;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Service;

public class ServiceTemplate extends ResourceTemplate<V1Service> {

    public ServiceTemplate(String name) {
        super(name);
    }

    @Override
    String type() {
        return "services";
    }

    @Override
    void passToCluster(V1Service parsed, CoreV1Api kube) throws ApiException {
        kube.createNamespacedService(parsed.getMetadata().getNamespace(), parsed, null, null, null);
    }
}
