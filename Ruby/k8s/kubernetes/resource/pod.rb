## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require_relative "../resource"
require 'filesize'

# Represents a set of Docker containers in a Kubernetes cluster.
class Pod < Resource

    # Run a block of code everything a message in logged in a container.
    def watch_container!(container_name=containers.first.name, &block)
        cluster.watch_pod_log(name, namespace, container: container_name, previous: true).each(block)
    end

    # Get the list of labels for this pod.
    def labels
        metadata.labels.to_h.map{|k,v| [k.to_s.to_sym, v.to_s]}.to_h
    end

    # Patch new labels to the pod.
    def label(values)
        cluster.patch_pod(id, {metadata: {labels: values}}, namespace)
    end

    def fetch!
        cluster.get_pod(id, namespace)
    end

    def total_size
      begin
        spec.containers.sum do |c|
          raise "ignored" if (c.resources.requests.nil? || c.resources.requests.memory.nil?) &&
                              (c.resources.limits.nil? || c.resources.limits.memory.nil?)

          if c.resources.limits.present?
            Filesize.from(c.resources.limits.memory).to_f("MB").ceil
          else
            Filesize.from(c.resources.requests.memory).to_f("MB").ceil * 2
          end
        end
      rescue
        # Some system pods don't have size requests/limits, so we give them a gig to be safe
        1000
      end
    end

    def destroy!
        cluster.delete_pod(id, namespace)
    end
end
