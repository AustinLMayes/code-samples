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
