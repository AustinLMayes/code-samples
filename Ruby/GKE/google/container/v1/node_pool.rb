require_relative "../kubernetes"

module Google
  module Container
    module V1
      class NodePool
        # Get the kubernetes nodes that make up this cluster
        def kube_nodes(all)
          all.select {|node| node.labels[:'cloud.google.com/gke-nodepool'] == id}
        end

        # Get the pods running on this cluster
        def pods(all)
          res = []
          kube_nodes(all).each do |node|
            res << node.pods
          end
          res.flatten
        end

      end
    end
  end
end
