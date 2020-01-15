## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require_relative "../lib/worker"

class NodeDeletionWorker < Worker
  include Kubernetes
  include Google

  def initialize()
    super("NodeDeleter", 1.minute)
  end

  def execute(itteration)
    google_cluster.node_pools.each do |pool|
      next if pool.nodes.empty? # This happens during creation/deletion - so we ignore it
      if pool.pods(nodes).empty? &&
        pool.nodes.first.created_at < Time.now - 5.minutes # Had time to schedule
        # No pods, delete
        log "Queued #{pool.name} for deletion..." unless pool.tags.include?("deletion-queued")
        pool.mark_for_deletion
      else
        # Could be marked and then used again
        pool.untag("deletion-queued")
      end
    end
  end
end
