package network.walrus.infrastructure.objects;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;

public class PodTemplate extends ResourceTemplate<V1Pod> {

  public PodTemplate(String name) {
    super(name);
  }

  @Override
  String type() {
    return "pods";
  }

  @Override
  void passToCluster(V1Pod parsed, CoreV1Api kube) throws ApiException {
    kube.createNamespacedPod(parsed.getMetadata().getNamespace(), parsed, null, null, null);
  }
}
