## BEGIN REPOSITORY HEADER ##

# The code contained in this file is provided without warranty, it was likely grabbed from a closed-source/abandoned project and will
# in most cases not function out of the box. This file is merely intended as a representation of the design pasterns and
# different problem-solving approaches I use to tackle various problems.
# The original file can be found here: N/A (Private Codebase)

## END REPOSITORY HEADER ##

require "kubeclient"
require "celluloid/current"
require "celluloid/io"

# Represents an object that interacts with the Kubernetes cluster.
module Kubernetes
    def cluster
        @cluster ||= begin
            cluster_internal
        rescue
            cluster_external
        end
    end

    def nodes
        cluster.get_nodes.map{|node| Node.new(node.metadata.name, nil)}
    end

    def has_service?(name)
      cluster.get_services.map{|s| s.metadata.name}.include?(name)
    end

    def delete_service(name, namespace)
      cluster.delete_service(name, namespace)
    end

    protected

    # Access the cluster from inside a pod that has a service account.
    def cluster_internal
        Kubeclient::Client.new(
            "https://kubernetes.default.svc",
            "v1",
            {
                ssl_options: {
                    ca_file: "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"
                },
                auth_options: {
                    bearer_token_file: "/var/run/secrets/kubernetes.io/serviceaccount/token"
                },
                socket_options: {
                    socket_class: Celluloid::IO::TCPSocket,
                    ssl_socket_class: Celluloid::IO::SSLSocket
                }
            }
        )
    end

    # Access the cluster from an external machine.
    def cluster_external
        config = Kubeclient::Config.read(File.expand_path("~/.kube/config"))
        context = config.context
        ssl_options = context.ssl_options
        ssl_options[:verify_ssl] = 0
        Kubeclient::Client.new(
            context.api_endpoint,
            context.api_version,
            {
                ssl_options: ssl_options,
                auth_options: context.auth_options,
                socket_options: {
                    socket_class: Celluloid::IO::TCPSocket,
                    ssl_socket_class: Celluloid::IO::SSLSocket
                }
            }
        )
    end
end
