## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require_relative "../lib/worker"
require_relative "../lib/kubernetes"
require_relative "../lib/cloudflare"

class PodCleaner < Worker
  include Kubernetes

  def initialize()
    super("PodCleaner", 1.minute)
  end

  def execute(itteration)
    nodes.each do |node|
      begin
        node.pods.each do |pod|
          if %w(Error Succeeded).include?(pod.status.phase)
            log "Deleting #{pod.name}..."
            pod.destroy!
          end
        end
      rescue Exception => e
        log("Failed to clean pods from #{node.name}! #{e}", :critical)
        puts e.backtrace
      end
    end
  end
end
