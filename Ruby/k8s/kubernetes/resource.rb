require_relative "api"
require_relative "../document"

# Represents a resource in a Kubernetes cluster.
class Resource
    include Kubernetes
    include Document

    def initialize(name, namespace)
        @id = name
        @namespace = namespace
        raise "Namespcae not provided" if (@namespace.nil? && !self.is_a?(Node))
    end

    # Get the name of the resource.
    def name
        id
    end

    # Get the cached namespace of the resource.
    def namespace
        @namespace
    end

    def stop_watching
      @watcher.finish if @watcher.present?
    end

    # Block the current thread and proccess a block of code for every resource update.
    def watch!(&block)
        @watcher = cluster.watch_events(namespace: namespace, field_selector: "involvedObject.name=#{name}", previous: false)
        @watcher.each do |notice|
          block.call notice
        end
    end

    def destroy!
        raise NotImplementedError, "Unable to delete a #{self.class.name} resource"
    end
end
