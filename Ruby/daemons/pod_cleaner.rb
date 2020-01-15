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
