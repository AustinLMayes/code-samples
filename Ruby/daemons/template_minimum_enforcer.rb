## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require_relative "../lib/worker"
require_relative "../lib/kubernetes"
require_relative "../lib/walrus"
require_relative "../listeners/server_creation_listener"

class TemplateMinimumEnforcer < Worker
  include Kubernetes
  include Walrus::ServerTemplates
  include Walrus::Servers

  def initialize()
    super("TemplateMinimumEnforcer", 1.minute)
  end

  def execute(itteration)
    all_templates.each do |template|
      next if template[:min_instances] < 1
      count = 0
      cluster.get_pods.each do |pod|
        count += 1 if pod.metadata.name.include? template[:identifier].to_s
      end
      next if count >= template[:min_instances]
      to_create = template[:min_instances] - count
      log "Creating #{to_create} pod(s) of #{template[:identifier]}..."
      to_create.times do
        ServerCreationListener.create_pod(create_server(template[:id]), template, cluster)
      end
    end
  end
end
