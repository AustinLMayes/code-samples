require_relative "resource_template"

module Templates
  class ServiceTemplate < ResourceTemplate
    def initialize(name)
      super("services", name)
    end

    def pass_to_cluster(cluster, resource)
      cluster.create_service(resource)
    end
  end
end
