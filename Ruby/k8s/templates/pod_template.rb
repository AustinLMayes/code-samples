require_relative "resource_template"
require_relative "../kubernetes/resource/pod"

module Templates
  class PodTemplate < ResourceTemplate
    def initialize(name)
      super("pods", name)
    end

    def pass_to_cluster(cluster, resource)
      cluster.create_pod(resource)
      return Pod.new(resource.metadata.name, resource.metadata.namespace)
    end
  end
end
