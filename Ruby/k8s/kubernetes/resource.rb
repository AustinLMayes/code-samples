## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

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
