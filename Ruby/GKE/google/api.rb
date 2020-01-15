require "google/cloud/container"

module Google
  def cluster_manager
    @cluster_manager ||= Google::Cloud::Container.new
  end

  def google_cluster
    @google_cluster ||= begin
      cluster_manager.get_cluster("walrus-network", "us-central1-a", "walrus-production")
    end
  end
end
