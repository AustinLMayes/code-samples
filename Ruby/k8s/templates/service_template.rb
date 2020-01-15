## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

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
