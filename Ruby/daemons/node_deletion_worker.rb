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
